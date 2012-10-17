/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.common.datamodel;

import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.EventServiceIdentityObject;
import netinf.common.datamodel.identity.GroupIdentityObject;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.identity.NodeIdentityObject;
import netinf.common.datamodel.identity.PersonIdentityObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.common.utils.DatamodelUtils;

/**
 * This is the factory that is responsible of creating all kinds of Objects concerning the datamodel.
 * <p>
 * The methods {@link DatamodelFactory#createAttributeFromBytes(byte[])},
 * {@link DatamodelFactory#createInformationObjectFromBytes(byte[])}, and
 * {@link DatamodelFactory#createIdentifierFromBytes(byte[])} are intended to deserialize the previously serialized objects of the
 * datamodel.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface DatamodelFactory {

   /**
    * @return a new {@link DataObject}.
    */
   DataObject createDataObject();

   /**
    * @return a new {@link Identifier} without any {@link IdentifierLabel}s. This is no valid {@link Identifier}.
    */
   Identifier createIdentifier();

   /**
    * @return a new {@link IdentifierLabel}. Every {@link IdentifierLabel} is only allowed to be added to at most one
    *         {@link Identifier}.
    */
   IdentifierLabel createIdentifierLabel();

   /**
    * @return a new {@link InformationObject}.
    */
   InformationObject createInformationObject();

   /**
    * @return a new {@link EventServiceIdentityObject}.
    */
   EventServiceIdentityObject createEventServiceIdentityObject();

   /**
    * @return a new {@link GroupIdentityObject}. A {@link GroupIdentityObject} defines the membership and the key distribution for
    *         members of the group.
    */
   GroupIdentityObject createGroupIdentityObject();

   /**
    * @return a new {@link IdentityObject}.
    */
   IdentityObject createIdentityObject();

   /**
    * @return a new {@link NodeIdentityObject}.
    */
   NodeIdentityObject createNodeIdentityObject();

   /**
    * @return a new {@link PersonIdentityObject}, which represents a natural person
    */
   PersonIdentityObject createPersonIdentityObject();

   /**
    * @return a new {@link ResolutionServiceIdentityObject}
    */
   ResolutionServiceIdentityObject createResolutionServiceIdentityObject();

   /**
    * @return a new {@link SearchServiceIdentityObject}
    */
   SearchServiceIdentityObject createSearchServiceIdentityObject();

   /**
    * This method creates an {@link Attribute}. The {@link Attribute#getAttributePurpose()} is set to "".
    * 
    * @param identification
    *           the identification of the returned {@link Attribute}
    * @param value
    *           the value of the returned {@link Attribute}
    * @return the new {@link Attribute}
    */
   Attribute createAttribute(String identification, Object value);

   /**
    * @return a new {@link Attribute}. An {@link Attribute} is only allowed to belong to at most one {@link InformationObject} or
    *         other {@link Attribute}.
    */
   Attribute createAttribute();

   /**
    * Creates a new copy of the given object. It is only possible to create copies for {@link Identifier},
    * {@link InformationObject} (and subclasses), {@link Attribute}, and {@link IdentifierLabel}.
    * 
    * This method is also called from {@link NetInfObjectWrapper#clone()} and {@link IdentifierLabel#clone()}.
    * 
    * @param <T>
    *           The object to be copied.
    * @param object
    *           The copied object. Only the datamodel related parts are copied.
    * @return The returned copy is unbound (does not have the same parent, if the paramater had one).
    */
   <T> T copyObject(T object);

   /**
    * Creates a new {@link Identifier} from the given {@code identifierString}. The format of the string can be seen at
    * {@link DatamodelUtils#getIdentifierLabels(String, DatamodelFactory)}.
    * 
    * @param identifierString
    * @return
    */
   Identifier createIdentifierFromString(String identifierString);

   /**
    * Creates a new {@link InformationObject} of the appropriate type from the given bytes array. This byte array had to be
    * created by a call to {@link InformationObject#serializeToBytes()}.
    * 
    * @param bytes
    * @return
    */
   InformationObject createInformationObjectFromBytes(byte[] bytes);

   /**
    * Creates a new {@link Attribute} from the given bytes array. This byte array had to be created by a call to
    * {@link Attribute#serializeToBytes()}.
    * 
    * @param bytes
    * @return
    */
   Attribute createAttributeFromBytes(byte[] bytes);

   /**
    * Creates a new {@link Attribute} from the given bytes array. This byte array had to be created by a call to
    * {@link Attribute#serializeToBytes()}.
    * 
    * @param bytes
    * @return
    */
   Identifier createIdentifierFromBytes(byte[] bytes);

   /**
    * Creates a {@link NetInfObjectWrapper} depending on the given byte array. This method mainly acts as a delegator for the
    * methods {@link DatamodelFactory#createInformationObjectFromBytes(byte[])},
    * {@link DatamodelFactory#createAttributeFromBytes(byte[])}, and {@link DatamodelFactory#createIdentifierFromBytes(byte[])}.
    * 
    * @param bytes
    * @return
    */
   NetInfObjectWrapper createFromBytes(byte[] bytes);

   /**
    * Tries to create a Datamodel object for a given class
    * 
    * @param <T>
    * @param clazz
    * @return
    */
   <T> T createDatamodelObject(Class<T> clazz);

   /**
    * Defines the kind of factory. The {@link SerializeFormat} is the one, to which the objects created by this factory are
    * serialized by calls to e.g. {@link NetInfObjectWrapper#serializeToBytes()}.
    * 
    * @return the serialization format of the objects created by this factory.
    */
   SerializeFormat getSerializeFormat();
}
