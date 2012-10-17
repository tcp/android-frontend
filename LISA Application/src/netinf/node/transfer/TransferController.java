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

import java.util.List;

import netinf.common.messages.NetInfMessage;

/**
 * A {@link TransferController} manages the transfer of Bitlevel Objects. For this it makes use of {@link TransferService}s, and
 * delegates the task to transfer something to the appropriate {@link TransferService}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface TransferController {

   List<Class<? extends NetInfMessage>> getSupportedOperations();

   NetInfMessage processNetInfMessage(NetInfMessage netInfMessage);

   void addTransferService(TransferService transferService);

   void removeTransferService(TransferService transferService);

   /**
    * @param source
    * @param destination
    *           might be <code>null</code>. In this case, a {@link TransferService} is automatically determined.
    * @param toUse
    *           is not allowed to be <code>null</code>.
    * @return
    */
   ExecutableTransferJob startTransfer(String source, String destination, TransferService toUse);

   /**
    * This method does never determine a new the {@link TransferService} on its own. For that the method
    * {@link TransferController#startTransfer(String, String, TransferService)} with {@link TransferService} set to
    * <code>null</code> has to be used.
    * 
    * @param jobId
    * @param newDestination
    * @param proceed
    *           if set to <code>true</code> the {@link TransferService} <code>toUse</code> is ignored, since it is not possible to
    *           proceed and change the {@link TransferService}. In case of proceeding the old {@link TransferService} is reused.
    * @param toUse
    *           might be <code>null</code>, in which case the old {@link TransferService} is used. If not <code>null</code> and
    *           not old {@link TransferService} then <code>proceed == false</code>, which implies the start of a new Transfer.
    * @return
    */
   ExecutableTransferJob changeTransfer(String jobId, String newDestination, boolean proceed, TransferService toUse);

   ExecutableTransferJob getTransferJob(String jobId);
}
