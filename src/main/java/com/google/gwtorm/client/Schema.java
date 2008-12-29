// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gwtorm.client;

/**
 * Application database definition and top-level schema access.
 * <p>
 * Applications should extend this interface and declare relation methods for
 * each entity/table used. Relation methods must be marked with the
 * {@link Relation} annotation and return an interface extending {@link Access}.
 * At runtime the application extension of Schema will be automatically
 * implemented with a generated class, providing implementations of the Access
 * extensions from each of the declared relation methods.
 * <p>
 * Instances of a schema should be obtained through the
 * {@link com.google.gwtorm.jdbc.Database} class on a pure-JDBC implementation
 * and through <code>GWT.create()</code> on the GWT client side.
 * <p>
 * In the JDBC implementation each Schema instance wraps around a single JDBC
 * Connection object. Therefore a Schema instance has a 1:1 relationship with an
 * active database handle.
 * <p>
 * A Schema instance (as well as its returned Access instances) is not thread
 * safe. Applications must provide their own synchronization, or ensure that at
 * most 1 thread access a Schema instance (or any returned Access instance) at a
 * time. The safest mapping is 1 schema instance per thread, never shared.
 * <p>
 * For example the OurDb schema creates two tables (identical structure) named
 * <code>someFoos</code> and <code>otherFoos</code>:
 * 
 * <pre>
 * public interface FooAccess extends Access&lt;Foo, Foo.Key&gt; {
 *   &#064;PrimaryKey(&quot;key&quot;)
 *   Foo byKey(Foo.Key k) throws OrmException;
 * }
 * 
 * public interface OurDb extends Schema {
 *   &#064;Relation
 *   FooAccess someFoos();
 * 
 *   &#064;Relation
 *   FooAccess otherFoos();
 * }
 * </pre>
 */
public interface Schema {
  /**
   * Automatically create the database tables.
   * 
   * @throws OrmException tables already exist or create permission is denied.
   */
  void createSchema() throws OrmException;

  /**
   * Begin a new transaction.
   * <p>
   * Only one transaction can be in-flight at a time on any given Schema
   * instance. Applications must commit or rollback a previously created
   * transaction before beginning another transaction on the same Schema.
   * 
   * @return the new transaction.
   * @throws OrmException the schema has been closed or another transaction has
   *         already been begun on this schema instance.
   */
  Transaction beginTransaction() throws OrmException;

  /**
   * Close the schema and release all resources.
   */
  void close();
}