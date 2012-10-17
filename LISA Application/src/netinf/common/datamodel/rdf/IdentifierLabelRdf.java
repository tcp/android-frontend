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
package netinf.common.datamodel.rdf;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.utils.DatamodelUtils;

/**
 * This is a {@link IdentifierLabelRdf}, it might only be used within the rdf-implementation of the datamodel
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentifierLabelRdf implements IdentifierLabel {

   private String labelName;
   private String labelValue;
   private IdentifierRdf identifierRdf;
   private final DatamodelFactoryRdf datamodelFactory;

   public IdentifierLabelRdf(DatamodelFactoryRdf datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
   }

   @Override
   public DatamodelFactory getDatamodelFactory() {
      return datamodelFactory;
   }

   @Override
   public String getLabelName() {
      return labelName;
   }

   @Override
   public String getLabelValue() {
      return labelValue;
   }

   @Override
   public void setLabelName(String labelName) {
      this.labelName = labelName;
      renameResourceIfBound();
   }

   @Override
   public void setLabelValue(String labelValue) {
      this.labelValue = labelValue;
      renameResourceIfBound();
   }

   @Override
   public Object clone() {
      return datamodelFactory.copyObject(this);
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((labelName == null) ? 0 : labelName.hashCode());
      result = prime * result + ((labelValue == null) ? 0 : labelValue.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      return DatamodelUtils.equalIdentifierLabels(this, obj);
   }

   @Override
   public int compareTo(IdentifierLabel arg0) {
      return DatamodelUtils.compareIdentifierLabels(this, arg0);
   }

   // ******** Internal Method ****** //

   public void renameResourceIfBound() {
      if (isBound()) {
         identifierRdf.renameResourceIfBound();
      }
   }

   private boolean isBound() {
      return this.identifierRdf != null;
   }

   public void setIdentifier(IdentifierRdf givenIdentifierRdf) {
      this.identifierRdf = givenIdentifierRdf;
   }

   public Identifier getIdentifier() {
      return identifierRdf;
   }

}
