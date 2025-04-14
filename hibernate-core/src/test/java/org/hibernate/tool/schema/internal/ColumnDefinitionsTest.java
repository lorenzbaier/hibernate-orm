/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.tool.schema.internal;

import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.mapping.Column;
import org.hibernate.orm.test.boot.models.BootstrapContextTesting;
import org.hibernate.testing.boot.ServiceRegistryTestingImpl;
import org.hibernate.tool.schema.extract.internal.ColumnInformationImpl;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertFalse;

public class ColumnDefinitionsTest {

	@Test
	void shouldNotMatchTimestampTypes() {
		var column = new Column( "test" );
		column.setSqlType( "timestamp(6) with time zone" );
		column.setSqlTypeCode( 3003 );
		// in postgres https://www.postgresql.org/docs/current/datatype-datetime.html
		// "timestamp" is equivalent to timestamp [ (p) ] [ without time zone ]
		var columnDef = new ColumnInformationImpl(
				null, null,
				93, "timestamp",
				29, 6, true );
		var dialect = new PostgreSQLDialect();
		var serveReg = ServiceRegistryTestingImpl.forUnitTesting();
		var metaOpt =new MetadataBuilderImpl.MetadataBuildingOptionsImpl(serveReg);
		var ctx = new BootstrapContextTesting(null, serveReg, metaOpt);
		metaOpt.setBootstrapContext(ctx);
		var meta = new MetadataImpl(
				UUID.randomUUID(),
				metaOpt,
				emptyMap(),
				emptyList(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				emptyMap(),
				new Database( metaOpt, null),
				ctx
		);

		assertFalse( ColumnDefinitions.hasMatchingType( column, columnDef, meta, dialect ) );
	}
}
