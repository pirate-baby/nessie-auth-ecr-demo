## Nessie Auth'ed in ECR Demo

This is a working example of an end-to-end Nessie deployment to AWS ECR, with both a local human client (via Jupyter) and a machine client (ie service or EL).

## Supported IDP Examples

<ul>
    <li>
        <a href="provider_resources/microsoft_entra/README.md">
            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Microsoft_logo.svg/1024px-Microsoft_logo.svg.png" width="25"> Microsoft Entra
        </a>
    </li>
</ul>

## What does this solve?
Nessie utilizes OpenID auth to determine both login identity (_authentication_) and permissions (_authorization_). This is a great practice, but can add great complexity to Iceberg clients that now need to support an Oauth2 flow. Enter the setup in this repo.

## How does it work?
OpenID identification and access tokens are highly portable by design, and we will leverage this to create a single auth system that can be used for both local and peer ECR container auth by leveraging the [locknessie](https://github.com/piratebaby/lock-nessie) package. This simplifies the middleware of OIDC security, but does not remove the complexity in the Data Lakehouse components. That complexity includes:

- The [CEL](https://cel.dev/) based RBAC
- The ID Provider (IDP) configurations for OIDC flows, roles and groups, allowed URIs, and audiences
- The storage provider methods of authentication & authorization (vended credentials, request signing etc)

Every combination of storage and IDP adds nuanced "gotchas" to a Nessie-catalog-based Iceberg data lakehouse. The goal in this repo is to document examples to help avoid the gotchas in deployment.

## What is the business case?
We need to be able to control data access to a Nessie-backed Iceberg Data Lake in the same way we'd expect control of any data warehouse. For example:
- Normal users are able to query the data lake (via Nessie) from their laptops and:
    - Read all data in `main`
    - Create new feature branches
    - Read and write to all non-main branches
    All using Python.
- a service role used for ETL must be able to both read and write to Nessie main from another container in the same ECR cluster.
In this example we designate the permissions as:
- `main.Read` can read all objects in `main`
- `main.All` can read/write/update/delete in `main`
- `feature.All` can CRUD non-main (feature) branches

### Components: local edition
To first build the stack on a laptop the parts are:
- Nessie
- Postgres (nessie backend)
- Minio (simulates S3)
- Jupyter Notebook (simulating local access via analytics team using `nessie-autoauth`)
- KeyCloak (OpenID IDP)
- el_script (Python script simulating your EL processes, such as Airbyte)

### Components: AWS edition
\# TODO: expand this as taskdef is completed
- ECS Cluster:
    - Nessie
    - KeyCloak
- Route 53
- ALB
- VPN
