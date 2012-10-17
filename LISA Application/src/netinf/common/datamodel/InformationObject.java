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

import java.io.Serializable;
import java.util.List;

import netinf.common.datamodel.attribute.Attribute;
import netinf.common.utils.DatamodelUtils;

/**
 * This is an {@link InformationObject}. It can be seen as a container for {@link Attribute}s and holds exactly one
 * {@link Identifier}. The {@link Attribute}s are always returned according to the order defined by
 * {@link DatamodelUtils#compareAttributes(Attribute, Attribute)}. IMPORTANT: This interface is not allowed to implement
 * {@link Serializable}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface InformationObject extends NetInfObjectWrapper {

   /*** Identifier ***/

   /**
    * Have a look at the constraint given in the "See Also" section.
    * 
    * @see InformationObject#setIdentifier(Identifier).
    * @return the identifier of this {@link InformationObject}.
    */
   Identifier getIdentifier();

   /**
    * An instance of the type {@link Identifier} can only belong to exactly one {@link InformationObject}. If the set
    * {@link Identifier} is attached to another {@link InformationObject} then it is removed from this other
    * {@link InformationObject}.
    * 
    * @param identifier
    */
   void setIdentifier(Identifier identifier);

   /*** Attributes ***/

   /**
    * This method searches for the first {@link Attribute} with the given <code>attributeIdentification</code>. Thus, if more than
    * one {@link Attribute} belongs to this {@link InformationObject} an arbitrary one is returned.
    * 
    * @param attributeIdentification
    *           The URI of the {@link Attribute}.
    * @return
    */
   Attribute getSingleAttribute(String attributeIdentification);

   /**
    * This list of attributes has to be sorted.
    * 
    * @param attributeIdentification
    *           The URI of the {@link Attribute}s.
    * @return a sorted list of {@link Attribute}s with the given {@code attributeIdentification}.
    */
   List<Attribute> getAttribute(String attributeIdentification);

   /**
    * An attribute can only belong to at most one parent, either to most one {@link InformationObject} or to at most one
    * {@link Attribute}. It is not allowed to be reused across different {@link InformationObject}s or {@link Attribute}s.
    * Accordingly, in case that the given {@link Attribute} does already belong to another "parent", it is removed from the
    * according list, and added to the given list. The value {@link Attribute#getInformationObject()} is set automatically within
    * this method.
    * <p>
    * A precondition for adding an {@link Attribute} is that the methods {@link Attribute#setIdentification(String)}
    * {@link Attribute#setValue(Object)} and {@link Attribute#setAttributePurpose(String)} were used successfully.
    * 
    * @param attribute
    *           This attribute is added to the list of attributes of the {@link InformationObject}.
    */
   void addAttribute(Attribute attribute);

   /**
    * Only {@link Attribute}s can be removed which are part of this {@link InformationObject}. Other {@link Attribute}s are simply
    * ignored. An {@link Attribute} that was removed has no parent (either {@link Attribute}, or {@link InformationObject}.
    * 
    * @param attribute
    *           The attribute to be removed.
    */
   void removeAttribute(Attribute attribute);

   /**
    * Removes all {@link Attribute}s with this {@code attributeIdentification}.
    * 
    * @param attributeIdentification
    */
   void removeAttribute(String attributeIdentification);

   /**
    * The returned list adheres to the ordering given in the "See Also" Section.
    * 
    * @see DatamodelUtils#compareAttributes(Attribute, Attribute)
    * @return Immutable list of {@link Attribute}s of this {@link InformationObject}.
    */
   List<Attribute> getAttributes();

   /**
    * All {@link Attribute}s with the given <code>attributePurpose</code> are returned.
    * 
    * @param attributePurpose
    * @return A possibly empty list of {@link Attribute}s. Will never return null.
    */
   List<Attribute> getAttributesForPurpose(String attributePurpose);

   // TODO: Think about the suitability of this place for the given method
   List<Identifier> getReaderIdentifiers();

   // TODO: Think about the suitability of this place for the following method
   List<String> getWriterPaths();

   /**
    * Returns a textual description ("I am receiving "+describe())
    * 
    * @return textual description
    */
   String describe();
}
