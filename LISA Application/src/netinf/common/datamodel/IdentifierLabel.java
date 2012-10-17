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

import netinf.common.datamodel.attribute.Attribute;

/**
 * The {@link IdentifierLabel} is one entry within the {@link Identifier}. It can be seen as a simple name-value pair.
 * <p>
 * Every {@link IdentifierLabel} is only allowed to belong to one {@link Identifier}. In case it is added to another identifier,
 * it is removed from the first one.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface IdentifierLabel extends Comparable<IdentifierLabel> {

   /**
    * @return
    */
   String getLabelName();

   /**
    * Sets the name of this label.
    * 
    * @see DefinedLabelName
    * @param labelName
    */
   void setLabelName(String labelName);

   String getLabelValue();

   /**
    * The label names and the label values have to fit to each other. An example for this correlation is
    * {@link DefinedLabelName#VERSION_KIND}, which can only have the values defined in {@link DefinedVersionKind}.
    * 
    * @param labelValue
    */
   void setLabelValue(String labelValue);

   /**
    * If an object is cloned, it is not bound to its parent. E.g. in case of a cloned {@link Attribute} this implies that,
    * whenever only a single {@link Attribute} is cloned, the cloned {@link Attribute} is not bound to any
    * {@link InformationObject} or {@link Attribute}.
    * 
    * @see DatamodelFactoryAbstract#copyObject(Object)
    * @return
    */
   Object clone();

   /**
    * If this {@link IdentifierLabel} has a parent ({@link Identifier}), the parent is NOT taken into account during the
    * calculation of the hashCode.
    * 
    * @return
    */
   int hashCode();

   /**
    * If this {@link IdentifierLabel} has a parent ({@link Identifier}), the parent is NOT compared during the calculation of
    * equality.
    * 
    * @param obj
    * @return
    */
   boolean equals(Object obj);

   /**
    * @return the datamodelFactory, which created the according objects.
    */
   DatamodelFactory getDatamodelFactory();

   int compareTo(IdentifierLabel arg0);
}
