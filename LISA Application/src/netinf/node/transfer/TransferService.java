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
package netinf.node.transfer;

/**
 * This is a transfer service, which gets an attribute that does contains the source from where the download has to be initiated.
 * And additionally, it receives a position where the download has to be stored/forwarded. Finally, we have the possibility to
 * change the destination to where a download is forwarded.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface TransferService {

   /**
    * The destination might be <code>null</code>. In that case, the {@link TransferService} determines the destination on its own.
    * Is intended to start the {@link ExecutableTransferJob} via the method {@link ExecutableTransferJob#startTransferJob()}.
    * 
    * @param source
    * @param destination
    *           <code>null</code>-able.
    * @return
    */
   ExecutableTransferJob startTransfer(String jobIdToUse, String source, String destination);

   /**
    * @param jobId
    * @param destination
    * @param proceed
    * @return
    */
   ExecutableTransferJob changeTransfer(String jobId, String newDestination, boolean proceed);

   boolean stopTransfer(String jobId);

   boolean containsTransferJob(String jobId);

   ExecutableTransferJob getTransferJob(String jobId);

   /**
    * It does not make sense to build the Identity of a {@link TransferService} on top of the NetInf-Infrastructure, since
    * transfer services are never distributed, and are only local to one NetInfNode. Thus the different {@link TransferService}
    * can be distinguished by a simple {@link String}.
    * 
    * @return
    */
   String getIdentity();

   /**
    * Returns a textual representation ("Can transfer via "+describe())
    * 
    * @return textual description
    */
   String describe();
}
