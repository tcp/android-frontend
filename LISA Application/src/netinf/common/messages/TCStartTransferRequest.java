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
package netinf.common.messages;

/**
 * <p>
 * This {@link NetInfMessage} starts the transfer from a given {@link TCStartTransferRequest#getSource()} to a given
 * {@link TCStartTransferRequest#getDestination()}. If the destination is not given (i.e. <code>null</code> is given as
 * destination) then the destination is determined automatically by the TransferService.
 * </p>
 * <p>
 * The {@link TCStartTransferRequest#getTransferServiceToUse()} is an identification for a TransferService that should be used. If
 * <code>null</code> is given, the TransferController determines the TransferService which is used.
 * </p>
 * <p>
 * The response to this message is the {@link TCStartTransferResponse}.
 * </p>
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see TCStartTransferResponse
 */
public class TCStartTransferRequest extends NetInfMessage {
   private String source;
   private String destination;
   private String transferServiceToUse;

   public String getTransferServiceToUse() {
      return transferServiceToUse;
   }

   public void setTransferServiceToUse(String transferServiceToUse) {
      this.transferServiceToUse = transferServiceToUse;
   }

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public String getDestination() {
      return destination;
   }

   public void setDestination(String destination) {
      this.destination = destination;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((destination == null) ? 0 : destination.hashCode());
      result = prime * result + ((source == null) ? 0 : source.hashCode());
      result = prime * result + ((transferServiceToUse == null) ? 0 : transferServiceToUse.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
     
      if (!super.equals(obj)) {
         return false;
      }
     
      TCStartTransferRequest other = (TCStartTransferRequest) obj;
      if (destination == null) {
         if (other.destination != null) {
            return false;
         }
      } else if (!destination.equals(other.destination)) {
         return false;
      }
      if (source == null) {
         if (other.source != null) {
            return false;
         }
      } else if (!source.equals(other.source)) {
         return false;
      }
      if (transferServiceToUse == null) {
         if (other.transferServiceToUse != null) {
            return false;
         }
      } else if (!transferServiceToUse.equals(other.transferServiceToUse)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nSource: " + source);
      stringBuilder.append("\nDestination: " + destination);
      stringBuilder.append("\nTransferServiceToUse: " + transferServiceToUse);

      return stringBuilder.toString();
   }

}
