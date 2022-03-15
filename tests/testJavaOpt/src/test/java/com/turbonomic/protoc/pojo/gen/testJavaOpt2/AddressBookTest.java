/*
 * Copyright (C) 2009 - 2022 Turbonomic, Inc.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.turbonomic.protoc.pojo.gen.testjavaopt2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.turbonomic.protoc.pojo.gen.addresses.Addresses;
import com.turbonomic.protoc.pojo.gen.addresses.Addresses.Person;
import com.turbonomic.protoc.pojo.gen.addresses.Addresses.Person.PhoneNumber;
import com.turbonomic.protoc.pojo.gen.addresses.Addresses.Person.PhoneType;
import com.turbonomic.protoc.pojo.gen.addresses.AddressesPOJO;
import com.turbonomic.protoc.pojo.gen.addresses.AddressesPOJO.PersonImpl;
import com.turbonomic.protoc.pojo.gen.addresses.AddressesPOJO.PersonImpl.PhoneNumberImpl;
import com.turbonomic.protoc.pojo.gen.addresses.AddressesPOJO.PersonView;

/**
 * Tests for code generated for address book proto.
 */
public class AddressBookTest {
    /**
     * testCopy.
     */
    @Test
    public void testCopy() {
        final PersonImpl person = new PersonImpl().setId(2).setEmail("person@email.com");
        final PersonImpl copy = person.copy();

        assertEquals(2, copy.getId());
        assertEquals("person@email.com", copy.getEmail());
        assertEquals(person, copy);
        assertEquals(person.hashCode(), copy.hashCode());
    }

    /**
     * testCopyConstructor.
     */
    @Test
    public void testCopyConstructor() {
        final PersonImpl person = new PersonImpl().setId(2).setEmail("person@email.com");
        final PersonImpl copy = new PersonImpl(person);

        assertEquals(2, copy.getId());
        assertEquals("person@email.com", copy.getEmail());
        assertEquals(person, copy);
        assertEquals(person.hashCode(), copy.hashCode());
    }

    /**
     * testEquality.
     */
    @Test
    public void testEquality() {
        final PersonImpl person1 = new PersonImpl().setId(1).setEmail("person1@email.com");
        final PersonImpl person2 = new PersonImpl().setId(2).setEmail("person2@email.com");
        final PersonImpl identicalToPerson1 = new PersonImpl().setId(1).setEmail("person1@email.com");

        assertNotEquals(person1, person2);
        assertEquals(person1, identicalToPerson1);
    }

    /**
     * testToProto.
     */
    @Test
    public void testToProto() {
        final PersonImpl pojo = new PersonImpl().setId(1).setEmail("person@email.com");
        assertEquals(
            Addresses.Person.newBuilder()
                .setId(1)
                .setEmail("person@email.com")
                .build(),
            pojo.toProto());
    }

    /**
     * testFromProto.
     */
    @Test
    public void testFromProto() {
        final Addresses.Person.Builder builder = Addresses.Person.newBuilder()
            .setId(1)
            .setEmail("person@email.com");

        assertEquals(PersonImpl.fromProto(builder), PersonImpl.fromProto(builder.build()));
    }

    /**
     * testToByteArray.
     */
    @Test
    public void testToByteArray() {
        final Addresses.Person proto = Addresses.Person.newBuilder()
            .setId(1)
            .setEmail("person@email.com")
            .build();
        final PersonImpl pojo = new PersonImpl().setId(1).setEmail("person@email.com");

        assertArrayEquals(proto.toByteArray(), pojo.toByteArray());
    }

    /**
     * Test for demo!.
     */
    @Test
    public void demoTest() {
        // Create a Person proto builder and convert to a POJO.
        final Person.Builder personBuilder = Person.newBuilder()
            .setId(1)
            .setName("David");
        final Person built = personBuilder.build();

        final PersonImpl person = PersonImpl.fromProto(personBuilder);
        final PersonView personView = person;

        // Access an optional field.
        assertEquals("David", person.getName());

        // Set an optional field. Show that the value has changed.
        person.setEmail("david@turbonomic.com");
        assertEquals("david@turbonomic.com", person.getEmail());

        // Convert back to proto and show the equivalence.
        personBuilder.setEmail("david@turbonomic.com");
        assertEquals(personBuilder.build(), person.toProto());

        // Add a new element to a repeated field.
        person.addPhones(new PhoneNumberImpl().setNumber("123"));

        // Show that default values work (PhoneType).
        assertEquals(PhoneType.MOBILE, person.getPhones(0).getType());

        // Change the default value. Add a new field (employer) and recompile.
        person.setEmployer("Turbo");
    }
}
