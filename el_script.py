from logging import getLogger, DEBUG, basicConfig
from locknessie.main import LockNessie
from pyiceberg.catalog import load_catalog
from pyiceberg.schema import Schema
from pyiceberg.types import NestedField, IntegerType, StringType, DoubleType
import pyarrow as pa

logger = getLogger(__name__)
basicConfig(level=DEBUG)


NAMESPACE = "demo"
TABLE_NAME = "sailboats"
FULLPATH = f"{NAMESPACE}.{TABLE_NAME}"

lnessie = LockNessie(auth_type="daemon")

nessie_host = "http://nessie:19120"
responses = []
catalog = load_catalog(
    "nessie",
    **{
        "uri": "http://nessie:19120/iceberg/main/",
        "token": lnessie.get_token(),
        "header.X-Iceberg-Access-Delegation": "remote-signing",
        "s3.session-token": "placeholder",
    }
)

logger.info("namespaces: %s", catalog.list_namespaces())

catalog.create_namespace(NAMESPACE)
tables = catalog.list_tables(NAMESPACE)
if TABLE_NAME in tables:
    logger.info("deleting table")
    catalog.delete_table(FULLPATH)
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

data = pa.table.from_pydict({
    "id": [1, 2, 3],
    "name": ["Sailboat 1", "Sailboat 2", "Sailboat 3"],
    "sailplan": ["Sloop", "Catamaran", "Ketch"],
    "draft": [2.5, 3.0, 2.8],
}, schema)

logger.info("inserting data into table")
catalog.insert_data(FULLPATH, data)
logger.info("data inserted")