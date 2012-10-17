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
package netinf.common.datamodel.impl;

import java.util.Iterator;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;

/**
 * This is a {@link DataObjectImpl}, it might only be used within the impl-implementation of the datamodel
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class DataObjectImpl extends InformationObjectImpl implements DataObject {

   private static final long serialVersionUID = -3782177062578702474L;

   public DataObjectImpl(DatamodelFactoryImpl datamodelFactory) {
      super(datamodelFactory);
   }

   @Override
   public List<Attribute> getLocatorByLocatorType(String locatorType) {
      List<Attribute> result = getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());

      Iterator<Attribute> iterator = result.iterator();

      while (iterator.hasNext()) {
         Attribute attribute = iterator.next();

         if (!attribute.getIdentification().equals(locatorType)) {
            iterator.remove();
         }
      }

      return result;
   }

   @Override
   public String describe() {
      StringBuffer buf = new StringBuffer("a Data Object that ");
      buf.append(getIdentifier().describe());
      return buf.toString();
   }

}
