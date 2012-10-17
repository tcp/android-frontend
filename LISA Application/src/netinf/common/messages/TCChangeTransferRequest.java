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
 * This {@link NetInfMessage} is responsible for changing a TransferService. It is possible to change the
 * {@link TCChangeTransferRequest#getNewDestination()} and the actual TransferService that is used, via
 * {@link TCChangeTransferRequest#getTransferServiceToUse()}.
 * </p>
 * <p>
 * If the {@link TCChangeTransferRequest#getNewDestination()} is <code>null</code> the new destination is determined
 * automatically.
 * </p>
 * <p>
 * It is not possible to select a TransferService within the {@link TCChangeTransferRequest}, if the transfer should be proceeded
 * {@link TCChangeTransferRequest#isProceed()} == <code>true</code>. In that case the old TransferService is reused.
 * </p>
 * <p>
 * If {@link TCChangeTransferRequest#getTransferServiceToUse()} is <code>null</code>, the old TransferService is used, independent
 * of the {@link TCChangeTransferRequest#isProceed()}-flag.
 * </p>
 * <p>
 * The reply to this message is the {@link TCChangeTransferResponse}.
 * </p>
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see TCChangeTransferResponse
 */
public class TCChangeTransferRequest extends NetInfMessage {

   private String jobId;
   private String newDestination;
   private boolean proceed;
   private String transferServiceToUse;

   public String getJobId() {
      return jobId;
   }

   public void setJobId(String jobId) {
      this.jobId = jobId;
   }

   public String getNewDestination() {
      return newDestination;
   }

   public void setNewDestination(String newDestination) {
      this.newDestination = newDestination;
   }

   public boolean isProceed() {
      return proceed;
   }

   public void setProceed(boolean proceed) {
      this.proceed = proceed;
   }

   public void setTransferServiceToUse(String transferServiceToUse) {
      this.transferServiceToUse = transferServiceToUse;
   }

   public String getTransferServiceToUse() {
      return transferServiceToUse;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
      result = prime * result + ((newDestination == null) ? 0 : newDestination.hashCode());
      result = prime * result + (proceed ? 1231 : 1237);
      result = prime * result + ((transferServiceToUse == null) ? 0 : transferServiceToUse.hashCode());
      return result;
   }

   /**
    * The overridden equals method. It generally holds: For any non-null reference value x, x.equals(null) should return false.
    * That check is implemented in the NetInfMessage class
    * 
    * @param obj
    *           The object comparing the message to. If it's not of the same type, the superclass method will take care of it
    * @see NetInfMessage
    */
   @Override
   public boolean equals(Object obj) {
      if (!super.equals(obj)) {
         return false;
      }
      /*
       * Both pointless, as the parent class function will catch this case if (getClass() != obj.getClass()) { return false; if
       * (this == obj) { return true; } }
       */
      // We are at this point certain
      TCChangeTransferRequest other = (TCChangeTransferRequest) obj;
      if (jobId == null) {
         if (other.jobId != null) {
            return false;
         }
      } else if (!jobId.equals(other.jobId)) {
         return false;
      }
      if (newDestination == null) {
         if (other.newDestination != null) {
            return false;
         }
      } else if (!newDestination.equals(other.newDestination)) {
         return false;
      }
      if (proceed != other.proceed) {
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

      stringBuilder.append("\nJobId: " + jobId);
      stringBuilder.append("\nNew Destination: " + newDestination);
      stringBuilder.append("\nProceed: " + proceed);
      stringBuilder.append("\nTransferServiceToUse: " + transferServiceToUse);

      return stringBuilder.toString();
   }
}
