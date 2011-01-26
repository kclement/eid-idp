/*
 * eID Identity Provider Project.
 * Copyright (C) 2010 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package test.unit.be.fedict.eid.idp.entity;

import be.fedict.eid.idp.entity.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ejb.Ejb3Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class PersistenceTest {

        private static final Log LOG = LogFactory.getLog(PersistenceTest.class);

        private EntityManager entityManager;

        @Before
        public void setUp() throws Exception {
                Class.forName("org.hsqldb.jdbcDriver");
                Ejb3Configuration configuration = new Ejb3Configuration();
                configuration.setProperty("hibernate.dialect",
                        "org.hibernate.dialect.HSQLDialect");
                configuration.setProperty("hibernate.connection.driver_class",
                        "org.hsqldb.jdbcDriver");
                configuration.setProperty("hibernate.connection.url",
                        "jdbc:hsqldb:mem:beta");
                configuration.setProperty("hibernate.hbm2ddl.auto", "create");

                configuration.addAnnotatedClass(AdministratorEntity.class);
                configuration.addAnnotatedClass(ConfigPropertyEntity.class);
                configuration.addAnnotatedClass(AppletConfigEntity.class);

                configuration.addAnnotatedClass(AttributeProtocolUriEntity.class);
                configuration.addAnnotatedClass(AttributeEntity.class);
                configuration.addAnnotatedClass(RPEntity.class);
                configuration.addAnnotatedClass(RPAttributeEntity.class);

                EntityManagerFactory entityManagerFactory = configuration
                        .buildEntityManagerFactory();

                this.entityManager = entityManagerFactory.createEntityManager();
                this.entityManager.getTransaction().begin();
        }

        @After
        public void tearDown() throws Exception {
                EntityTransaction entityTransaction = this.entityManager
                        .getTransaction();
                LOG.debug("entity manager open: " + this.entityManager.isOpen());
                LOG.debug("entity transaction active: " + entityTransaction.isActive());
                if (entityTransaction.isActive()) {
                        if (entityTransaction.getRollbackOnly()) {
                                entityTransaction.rollback();
                        } else {
                                entityTransaction.commit();
                        }
                }
                this.entityManager.close();
        }

        @Test
        public void testCorrectNamedQueries() throws Exception {
                // empty
        }
}
