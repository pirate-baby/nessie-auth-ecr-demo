## Nessie Auth'ed in ECR Demo

This is a working example of an end-to-end Nessie deployment to AWS ECR, with both local client and Dremio (jn ECR or local) auth.

## What does this solve?
Nessie utilizes OpenID auth to determine both login identity (_authentication_) and permissions (_authorization_). This is a great practice, but can add a great complexity to Iceberg clients that now need to support an Oauth2 flow. Enter the setup in this repo.

## How does it work?
OpenID identification and access tokens are highly portable by design, and we will leverage this to create a single auth system that can be used for both local and peer ECR container auth by using [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/). The flow goes as follows:
- A simple web auth app (hosted in ECR) provides endpoints for the Oauth2 sign-in flow.
- The token created in the flow is pushed to a distinct secret in AWS Secrets Manager (AWSSM).
- Clients read from the given AWSSM secret and add the token to Nessie requests as a `Bearer: Token` header.
    - ECR peer clients use container role privileges
    - Local clients use local key/id privileges
- Token refreshes in the web auth app generate a new access token which is pushed to update AWSSSM.

## What is the business case?
We need to be able to control data access to a Nessie-backed Iceberg Data Lake in the same way we'd expect control of any data warehouse. For example:
- `analytics` users are able to query the data lake (via Nessie) from their laptops and:
    - Read all data in `main`
    - Create new branches
    - Read and write to all non-main branches
    All using Python.
- an `etl` OpenID role is able to read and write to Nessie main from another container in the same ECR cluster.
- a `dremio` OpenID role is able to read from Nessie main from a Dremio instance in the same ECR cluster.


### The OpenID elephant in the room
Like a lot of more modern applications, Nessie heavily favors OpenID for auth. If you are deploying Nessie in any kind of enterprise, you probably do to. But this is where a lot of the complexity enters in documenting a working demo; if you need your Azure Entra users to be set up with the correct roles etc to replicate a working example, that can mean involving security teams, changing your org access, all bad stuff. So for this demo we'll use [KeyCloak](https://www.keycloak.org/) to fill the OpenID server role. You then just need to adapt this code to your OpenID provider of choice. We will deploy this both locally _and_ in AWS to make sure our OID works everywhere.

### Components: local edition
To first build the stack on a laptop the parts are:
- Nessie
- Postgres (nessie backend)
- Minio (simulates S3)
- Dremio
- Jupyter Notebook (simulating local access via analytics team using `nessie-autoauth`)
- KeyCloak (OpenID)
- Nginx (reverse proxy)

### Components: AWS edition
- ECS Cluster:
    - Nessie
    - KeyCloak
    - Dremio
    - Nginx
- Route 53
- ALB
- VPN


