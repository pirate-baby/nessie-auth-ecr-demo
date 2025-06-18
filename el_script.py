"""Daemon Demo

This script is used to create a table in the Nessie catalog "main" branch, and populate it with some data.
Here we are authenticating as the application itself (ie daemon authentication) which requires no interactive user cycle.
Our application was granted `main.All` back in the IDP, so this auth token will allow us to delete the table, create a new namespace,
and both create and write to the table.

It is important to note that this script has no direct access to storage (in our case either MinIO or s3). If we make a boto3 call to the bucket it will fail.
The access is managed by Nessie via that `X-Iceberg-Access-Delegation` header. Each request from pyIceberg will get back a signed URL from Nessie that is good
for one action.

"""
from logging import getLogger, INFO, basicConfig
from locknessie.main import LockNessie
from pyiceberg.catalog import load_catalog
from pyiceberg.schema import Schema
from pyiceberg.types import NestedField, IntegerType, StringType, DoubleType
import pyarrow as pa


logger = getLogger(__name__)
basicConfig(level=INFO)


NAMESPACE = "demo"
TABLE_NAME = "sailboats" # sorry, I'm shopping for a new boat so you all need to live with my weird example data.
FULLPATH = f"{NAMESPACE}.{TABLE_NAME}"

lnessie = LockNessie(auth_type="daemon")

nessie_host = "http://nessie:19120"
responses = []
catalog = load_catalog(
    "nessie",
    **{
        "uri": "http://nessie:19120/iceberg/main/",
        "token": lnessie.get_token(),
        "header.X-Iceberg-Access-Delegation": "remote-signing", # this gets us signed URLs that work without any credentials
        "s3.session-token": "placeholder", # TODO: is this still needed? there was a bug that pyiceberg would error without a session token, but that may be fixed now.
    }
)

catalog.create_namespace_if_not_exists(NAMESPACE)
tables = catalog.list_tables(NAMESPACE)
if (NAMESPACE, TABLE_NAME,) in tables:
    logger.info("deleting table")
    catalog.drop_table(FULLPATH)
    logger.info("table deleted")

schema = Schema(
    NestedField(1, "id", IntegerType(), required=True),
    NestedField(2, "name", StringType(), required=False),
    NestedField(3, "sailplan", StringType(), required=False),
    NestedField(4, "draft", DoubleType(), required=False),
)

logger.info("creating table")
catalog.create_table(FULLPATH, schema)
logger.info("table created")

data = pa.Table.from_pydict({
    "id": [1, 2, 3],
    "name": ["1986 Bayfield 36", "1989 Brewer 44", "1983 Pearson 424"],
    "sailplan": ["Cutter", "Sloop", "Ketch"],
    "draft": [6.0, 5.9, 4.9],
}, pa.schema(schema.as_arrow()))

logger.info("inserting data into table")
table = catalog.load_table(FULLPATH)
table.append(data)
logger.info("data inserted")