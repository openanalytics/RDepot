FROM osixia/openldap
#ENV LDAP_ORGANISATION "Test Company"
#ENV LDAP_DOMAIN "example.org"
#ENV LDAP_ADMIN_PASSWORD "testpassword"
#ENV LDAP_TLS "false"
COPY ./import/config /etc/ldap/slapd.d/
COPY ./import/db /var/lib/ldap/
