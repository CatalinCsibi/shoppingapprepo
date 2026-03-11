How to run and configure Keycloak

1. Install Keycloak
   Go to:
   https://www.keycloak.org/downloads

    Download Keycloak (Quarkus distribution)

    Extract it:
    unzip keycloak-25.0.0.zip

    cd keycloak-25.0.0/bin

    Create admin user:
    ./kc.sh add-user --username admin --password admin --roles admin 


2. Start Keycloak:
   cd keycloak-25.0.0/bin and run ./kc.sh start-dev. 
    
   If the previous command does not work try using one of the following:

   a. KEYCLOAK_ADMIN=admin KEYCLOAK_ADMIN_PASSWORD=admin ./kc.sh start-dev
   
   b. export KEYCLOAK_ADMIN=admin export KEYCLOAK_ADMIN_PASSWORD=admin ./kc.sh start-dev


3. Login to Keycloak:

   Now open: http://localhost:8080
   
   Login: admin / admin


4. Create Realm

   Top left → dropdown → Create Realm: shopping-realm


5. Create Roles

   Go to: Realm → Roles → Create Role and Create: ADMIN


6. Create User

   Users → Create User

   Example:

   username: admin
   
   email verified: OFF
   
   Then: Credentials → Set Password → Temporary OFF


7. Assign Role

   Users → admin → Role Mapping → Assign Role → ADMIN


8. Create Client:
   
   a. Go to Clients → Create Client

   b. Click Clients -> Create client

   c. Client type: OpenID Connect / Client ID: shopping-app-client (example) / Click Next

   d. On next page toggle ON Client Authentication and Authorization and in Authentication Flow only select Standard flow. Click next

   e. On last page we will have to configure a Redirect URI. Put something like: http://localhost:8083/callback From this URI we will be able to retrieve the Authorization Code. Click Save

Connection to keycloak in our spring boot application has been configured in application.yaml:
Under spring:

    security:
        oauth2:
            resourceserver:
                jwt:
                    issuer-uri: http://localhost:8080/realms/shopping-realm