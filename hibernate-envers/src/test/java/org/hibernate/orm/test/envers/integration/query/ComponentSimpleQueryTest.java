/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.envers.integration.query;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.JoinType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.orm.test.envers.BaseEnversJPAFunctionalTestCase;
import org.hibernate.orm.test.envers.Priority;
import org.hibernate.testing.orm.junit.JiraKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Felix Feisst (feisst dot felix at gmail dot com)
 */
@SuppressWarnings("rawtypes")
@JiraKey(value = "HHH-11895")
public class ComponentSimpleQueryTest extends BaseEnversJPAFunctionalTestCase {


	@Entity(name = "Asset")
	@Audited
	public static class Asset {

		@Id
		@GeneratedValue
		private Integer id;

		@ElementCollection
		private Set<String> multiSymbols = new HashSet<>();

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Set<String> getMultiSymbols() {
			return multiSymbols;
		}

		public void setMultiSymbols(Set<String> multiSymbols) {
			this.multiSymbols = multiSymbols;
		}

	}

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[]{ Asset.class };
	}

	private Asset asset1;
	private Asset asset2;
	private Asset asset3;

	@Test
	@Priority(10)
	public void initData() {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();

		asset1 = new Asset();
		em.persist( asset1 );

		asset2 = new Asset();
		asset2.getMultiSymbols().add( "X" );
		em.persist( asset2 );

		asset3 = new Asset();
		asset3.getMultiSymbols().add( "Y" );
		asset3.getMultiSymbols().add( "X" );
		em.persist( asset3 );

		em.getTransaction().commit();
	}

	@Test
	public void testMultiSymbol() {
		List actual = getAuditReader().createQuery()
				.forEntitiesAtRevision( Asset.class, 1 )
				.traverseRelation( "multiSymbols", JoinType.INNER, "s" )
				.up()
				.addProjection( AuditEntity.id() )
				.addOrder( AuditEntity.id().asc() )
				.getResultList();
		List<Integer> expected = new ArrayList<>();
		Collections.addAll( expected, asset2.getId(), asset3.getId() );
		assertEquals( "Expected only the ids of the assets with symbol T1", expected, actual );
	}
}
