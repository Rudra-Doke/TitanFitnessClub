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

The repository has no Render service manifest or deployment URL, so this package cannot establish which commit or database your live service currently uses. Confirm the service's connected repository/branch and latest successful deploy in Render before deploying this copy. A database credential that was previously exposed in Git history has been rotated; it is not included in this package. Rewriting remote history to remove an old value would require a coordinated force-push and is not part of this patch.

### Online membership payments

Member registration and renewal payments can be completed through Razorpay Checkout. The server creates each order from the saved membership plan amount, verifies Razorpay's checkout signature, and checks with Razorpay that the payment is captured and matches the order amount before recording it as Paid. In-club payments can still be recorded by staff.

To enable checkout, add `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` to the Render web service environment. Start with Razorpay **test mode** keys and test cards. Keep the secret only in Render's environment settings; never add either value to source files or commit history. When the keys are absent, online payment buttons stay disabled and pending payments remain available for staff handling. Production/live payments require the account owner's explicit decision to switch to live keys.

For payment confirmation recovery when a member closes checkout before returning to the app, configure a Razorpay webhook after this version is deployed. Set its URL to `https://titanfitnessclub.onrender.com/webhooks/razorpay`, subscribe to `payment.captured`, and add the webhook's signing secret as `RAZORPAY_WEBHOOK_SECRET` in the linked Render environment group. The webhook secret is separate from `RAZORPAY_KEY_SECRET`; keep it private. Configure matching test-mode webhook settings while using test keys. The endpoint verifies Razorpay's signature and only records a captured INR payment when its saved order and amount match a pending payment.

## Security and behavior changes

- Web sessions expire after 30 minutes of inactivity. Session cookies are HTTP-only, secure, and use `SameSite=Lax`; forwarded HTTPS headers are recognized for Render's TLS proxy. Spring Security also rotates the session identifier after login and limits referrer data sent to other sites.
- `/member/**` requires the MEMBER role; other authenticated application routes require ADMIN.
- Disabled accounts are rejected by Spring Security.
- Member deactivation marks the member inactive and disables the matching login while retaining associated payment and attendance history.
- Registration writes the login, member, and pending payment in one transaction. It validates input and stores the fitness goal entered in the form.
- Revenue totals include only payments with status `Paid`; new registrations and renewals stay `Pending` until Razorpay confirms a captured payment or staff records an in-club payment.
- All delete actions use POST forms with Spring Security CSRF protection.
- Members can edit their own contact/profile details and change a password after confirming the current password.
- Members can request a renewal or plan change and pay pending registration/renewal records through Razorpay Checkout when keys are configured.
- Member membership dates are calculated from the most recent paid membership record and that plan's duration. Pending requests do not extend a membership.
- The admin search supports member name, email, and member ID. Dashboard shortcuts open the corresponding admin forms.
- The member management form uses the plans actually configured by staff rather than a fixed sample list. Adding a member record does not create login credentials; members should register to create their own password.
- Demo credentials and database secrets are not included in this copy.

## Verification

Regression tests cover role boundaries, disabled-account enforcement, POST/CSRF behavior, paid-only revenue, profile/password changes, member search, renewal requests, and member dashboard/membership rendering. Test configuration uses an in-memory H2 database. Run `mvnw test` (Windows: `mvnw.cmd test`) before deployment.
