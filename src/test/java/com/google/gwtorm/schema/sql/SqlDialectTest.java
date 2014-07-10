// Copyright (C) 2014 The Android Open Source Project
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

package com.google.gwtorm.schema.sql;

import static org.junit.Assert.assertEquals;

import com.google.gwtorm.data.Person;
import com.google.gwtorm.data.PhoneBookDb;
import com.google.gwtorm.data.PhoneBookDb2;
import com.google.gwtorm.jdbc.Database;
import com.google.gwtorm.jdbc.JdbcExecutor;
import com.google.gwtorm.server.OrmException;

import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class SqlDialectTest {
  protected JdbcExecutor executor;
  protected Connection db;
  protected SqlDialect dialect;
  protected Database<PhoneBookDb> phoneBook;
  protected Database<PhoneBookDb2> phoneBook2;

  @Test
  public void testRollbackTransaction() throws SQLException, OrmException {
    PhoneBookDb schema = phoneBook.open();
    schema.updateSchema(executor);
    schema.people().beginTransaction(null);
    ArrayList<Person> all = new ArrayList<>();
    all.add(new Person(new Person.Key("Bob"), 18));
    schema.people().insert(all);
    schema.rollback();
    List<Person> r = schema.people().olderThan(10).toList();
    assertEquals(0, r.size());
  }

  @Test
  public void testRollbackNoTransaction() throws Exception {
    PhoneBookDb schema = phoneBook.open();
    schema.updateSchema(executor);
    ArrayList<Person> all = new ArrayList<>();
    all.add(new Person(new Person.Key("Bob"), 18));
    schema.people().insert(all);
    schema.commit();
    schema.rollback();
    List<Person> r = schema.people().olderThan(10).toList();
    assertEquals(1, r.size());
  }

  @Test
  public void testCommitTransaction() throws Exception {
    PhoneBookDb schema = phoneBook.open();
    schema.updateSchema(executor);
    schema.people().beginTransaction(null);
    ArrayList<Person> all = new ArrayList<>();
    all.add(new Person(new Person.Key("Bob"), 18));
    schema.people().insert(all);
    schema.commit();
    List<Person> r = schema.people().olderThan(10).toList();
    assertEquals(1, r.size());
  }
}