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
package netinf.common.datamodel.attribute;

import java.util.List;

import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.ValueUtils;

/**
 * An {@link Attribute} mainly consists of four parts.
 * <ul>
 * <li>The identification of the {@link Attribute}, which has to be a valid URI pointing to an ontology, in which the semantic for
 * the {@link Attribute} is defined.</li>
 * <li>The value of the {@link Attribute}. This might be an arbitrary Java-Object, which has to implement the
 * {@link Object#toString()} method properly. The other translation way (from String to the according Object) is defined within
 * {@link ValueUtils}.</li>
 * <li>The attributePurpose of the {@link Attribute}. The attributePurpose might be an arbitrary {@link String}. Predefined
 * attributes are defined in {@link DefinedAttributePurpose}.</li>
 * <li>Finally, the fourth part of an {@link Attribute} is the list of subattributes.</li>
 * </ul>
 * 
 * The subattributes have to be ordered like defined by {@link DatamodelUtils#compareAttributes(Attribute, Attribute)}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface Attribute extends NetInfObjectWrapper, Comparable<Attribute> {

   /**
    * @see InformationObject#getSingleAttribute(String)
    * @param identification
    * @return
    */
   Attribute getSingleSubattribute(String identification);

   /**
    * @see InformationObject#getAttribute(String)
    * @param identification
    * @return
    */
   List<Attribute> getSubattribute(String identification);

   /**
    * @see InformationObject#addAttribute(Attribute)
    * @param attribute
    */
   void addSubattribute(Attribute attribute);

   /**
    * @see InformationObject#removeAttribute(Attribute)
    * @param attribute
    */
   void removeSubattribute(Attribute attribute);

   /**
    * @see InformationObject#removeAttribute(String)
    * @param identification
    */
   void removeSubattribute(String identification);

   /**
    * @see InformationObject#getAttributes()
    * @return
    */
   List<Attribute> getSubattributes();

   /**
    * Returns the {@link Attribute} to which this {@link Attribute} belongs. Accordingly this value is {@code null}, if this
    * {@link Attribute} does directly belong to an {@link InformationObject}.
    * 
    * There is no equivalent setParentAttribute. The parent attribute is automatically set in
    * {@link Attribute#addSubattribute(Attribute)}
    * 
    * @return
    */
   Attribute getParentAttribute();

   /**
    * Returns the {@link InformationObject} to which this {@link Attribute} belongs directly or indirectly.
    * 
    * There is no equivalent getInformationObject. The information object is automatically set/deduced in
    * {@link Attribute#addSubattribute(Attribute)}
    * 
    * @return
    */
   InformationObject getInformationObject();

   // Value

   /**
    * @param <T>
    * @param class1
    * @return the value of this {@link Attribute} as the type defined by {@code class1}.
    */
   <T> T getValue(Class<T> class1);

   /**
    * Sets the {@code object} as the new object of this {@link Attribute}. The object must be serializable via the
    * {@link Object#toString()} method, and deserializeable like defined in {@link ValueUtils}.
    * 
    * @param object
    */
   void setValue(Object object);

   /**
    * Determines the type of the value of this {@link Attribute}. The type name has to adhere to the following conventions given
    * in {@link DatamodelUtils#getValueType(Object)}.
    * 
    * @return the value type
    */
   String getValueType();

   /**
    * The raw string representing the the value of the {@link Attribute}. This has to following format:
    * {@link Attribute#getValueType()}, {@code attributeValue.toString()}, delimited by
    * {@link DatamodelUtils#TYPE_VALUE_SEPARATOR}.
    * 
    * @return
    */
   String getValueRaw();

   // Identification

   /**
    * The URI of this {@link Attribute}. The URI must point to a valid ontology, where the meaning of this attribute is defined.
    * 
    * @param uri
    */
   void setIdentification(String uri);

   /**
    * @return
    */
   String getIdentification();

   // Everything concerning the attribute-purpose

   String getAttributePurpose();

   void setAttributePurpose(String string);

   /**
    * @param attributePurpose
    * @return an immutable list of {@link Attribute}s, which have the given {@code attributePurpose}. The returned value is never
    *         {@code null}, but instead an empty list.
    */
   List<Attribute> getSubattributesForPurpose(String attributePurpose);

   int compareTo(Attribute arg0);
}
