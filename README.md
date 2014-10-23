Deployment Instructions
======================
The project used Glassfish 3.1 Java EE application server for deployment, with configured connection pooling to derby bundled Glassfish (i.e. starting derby from glassfish directory, using cmd commands: asadmin, start-database) database and JPA 2.0 or 2.1 with EclipseLink 2.5.x platform.
Additionally the project needs the libraries from Glassfish directory such as: javax.servlet.*, javax.persistence.*, etc.
