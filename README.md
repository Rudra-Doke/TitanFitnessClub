# Titan Fitness Club (patched)

This is a repaired copy of the top-level Spring Boot project from the supplied `TitanFitnessClub-main.zip`. The original archive and synced `sources/` files were left unchanged.

## Run locally

Use Java 21 and MySQL. Configure the connection with environment variables:

- `SPRING_DATASOURCE_URL` — for example, a JDBC MySQL URL pointing to your local database
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PORT` — optional; defaults to `8080`

The app starts with schema update enabled to preserve the project's existing setup. It now stores the registration fitness goal in a new nullable `members.fitness_goal` column; Hibernate adds that column when the app starts.

New member accounts are created through registration. There is no default admin or demo member password. To create the first admin on startup, set both `TITAN_SEED_ADMIN_USERNAME` and `TITAN_SEED_ADMIN_PASSWORD` in the environment. The app creates that admin only when the username does not exist. Remove the seed variables after the account is created; changing them later does not change an existing password.

## Render / Docker

The Dockerfile builds with Java 21 and runs the Maven tests before assembling the executable JAR. Configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` in the Render service environment. Use the JDBC URL and credentials for the database actually attached to the service. Render's assigned `PORT` is honored automatically.

The repository has no Render service manifest or deployment URL, so this package cannot establish which commit or database your live service currently uses. Confirm the service's connected repository/branch and latest successful deploy in Render before deploying this copy. The public repository currently exposes a database credential in its tracked configuration; rotate any credential that may still be valid, and remove secrets from future commits.

## Security and behavior changes

- `/member/**` requires the MEMBER role; other authenticated application routes require ADMIN.
- Disabled accounts are rejected by Spring Security.
- Member deactivation marks the member inactive and disables the matching login while retaining associated payment and attendance history.
- Registration writes the login, member, and pending payment in one transaction. It validates input and stores the fitness goal entered in the form.
- Revenue totals include only payments with status `Paid`; registration creates a `Pending` record because no payment processor is integrated.
- All delete actions use POST forms with Spring Security CSRF protection.
- Demo credentials and the tracked datasource password were removed from this copy.

## Verification

Regression tests cover role boundaries, disabled-account enforcement, POST/CSRF behavior, and paid-only revenue. Test configuration uses an in-memory H2 database. Run `mvnw test` (Windows: `mvnw.cmd test`) before deployment.
