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
package netinf.common.transfer;

/**
 * A {@link TransferJob} represents the transfer of a BitLevel Object (BO). It consists of a {@link TransferJob#getSource()}, a
 * {@link TransferJob#getDestination()} (both typically URLs), and a unique {@link TransferJob#getJobId()}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TransferJob {

   private String source;
   private final String jobId;
   private String destination;

   public TransferJob(String jobId, String source, String destination) {
      super();
      this.source = source;
      this.jobId = jobId;
      this.destination = destination;
   }

   public String getSource() {
      return source;
   }

   public String getDestination() {
      return destination;
   }

   public String getJobId() {
      return jobId;
   }

   protected void setSource(String source) {
      this.source = source;
   }

   protected void setDestination(String destination) {
      this.destination = destination;
   }

   @Override
   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();

      stringBuilder.append("TransferJobKind: " + getClass().getSimpleName());

      stringBuilder.append("\nJobId: " + jobId);
      stringBuilder.append("\nSource: " + source);
      stringBuilder.append("\nDestination: " + destination);

      return stringBuilder.toString();
   }

}
