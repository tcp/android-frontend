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

import java.util.List;

import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.utils.DatamodelUtils;

/**
 * This class represents an {@link Identifier}. An {@link Identifier} is a list of simple {@link IdentifierLabel}s. The
 * {@link IdentifierLabel}s are arranged in a particular order, according to the definition within {@link DefinedLabelName}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface Identifier extends NetInfObjectWrapper {

   /**
    * The sorting of the {@link IdentifierLabel}s has to adhere to
    * {@link DatamodelUtils#compareIdentifierLabels(IdentifierLabel, IdentifierLabel)}.
    * 
    * @return An immutable list of the {@link Identifier}s {@link IdentifierLabel}s. Changes to the given list do not affect this
    *         {@link Identifier}.
    */
   List<IdentifierLabel> getIdentifierLabels();

   /**
    * Adds the {@link IdentifierLabel} to this {@link Identifier}. If it does already belong to another {@link Identifier}, it is
    * removed from this other {@link Identifier}.
    * 
    * Every {@link IdentifierLabel} is only allowed to be added to at most one {@link Identifier}.
    * 
    * @param identifierLabel
    */
   void addIdentifierLabel(IdentifierLabel identifierLabel);

   /**
    * Removes the given {@link IdentifierLabel} from this {@link Identifier}, if the label is part of the identifier.
    * 
    * @param identifierLabel
    */
   void removeIdentifierLabel(IdentifierLabel identifierLabel);

   /**
    * Removes the {@link IdentifierLabel} with the given {@code labelName}. If no such label exists, nothing happens.
    * 
    * @param labelName
    */
   void removeIdentifierLabel(String labelName);

   /**
    * @param labelName
    * @return
    */
   IdentifierLabel getIdentifierLabel(String labelName);

   /**
    * @return the string representation of this {@link Identifier}, like defined by
    *         {@link DatamodelUtils#identifierToString(Identifier)}.
    */
   String toString();

   /**
    * Initialize this identifier from the given {@link String}.
    * 
    * @param string
    * @throws NetInfUncheckedException
    *            in case the given identifier is not properly formatted.
    * @deprecated Instead use {@link DatamodelFactory#createIdentifierFromString(String)}
    */
   @Deprecated
   void initFromString(String string);

   /**
    * @return whether the identifier is versioned or not. The definition of a version identifier can be found at
    *         {@link DatamodelUtils#isIdentifierVersioned(Identifier)}
    */
   boolean isVersioned();

   /**
    * Returns a textual representation ("An identifier that says that the IO is "+describe())
    * 
    * @return textual representation
    */
   String describe();
}
