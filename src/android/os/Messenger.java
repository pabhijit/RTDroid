/*
 * Copyright (C) 2006 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os;

import android.realtime.RTHandler;

/**
 * Reference to a Handler, which others can use to send messages to it. This
 * allows for the implementation of message-based communication across
 * processes, by creating a Messenger pointing to a Handler in one process, and
 * handing that Messenger to another process.
 */
public final class Messenger {
    private final IMessenger mTarget;

    /**
     * Create a new Messenger pointing to the given Handler. Any Message objects
     * sent through this Messenger will appear in the Handler as if
     * {@link Handler#sendMessage(Message) Handler.sendMessage(Message)} had
     * been called directly.
     * 
     * @param target The Handler that will receive sent messages.
     */
    public Messenger(Handler target) {
        mTarget = target.getIMessenger();
    }
    
    public Messenger(RTHandler target){
        mTarget = target.getIMessenger();
    }

    /**
     * Send a Message to this Messenger's Handler.
     * 
     * @param message The Message to send. Usually retrieved through
     *            {@link Message#obtain() Message.obtain()}.
     * @throws RemoteException Throws DeadObjectException if the target Handler
     *             no longer exists.
     */
    public void send(Message message) throws RemoteException {
        System.out.println("Messenger send meesage");
        mTarget.send(message);
    }
    
    public void send(RTMessage rtmsg) throws RemoteException {
    	System.out.println("Messenger send real-time meesage closure");
    	mTarget.send(rtmsg);
    }

    /**
     * Retrieve the IBinder that this Messenger is using to communicate with its
     * associated Handler.
     * 
     * @return Returns the IBinder backing this Messenger.
     */
    // TODO
    public IBinder getBinder() {
        return mTarget.asBinder();
    }

    /**
     * Comparison operator on two Messenger objects, such that true is returned
     * then they both point to the same Handler.
     */
    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        try {
            return mTarget.asBinder().equals(((Messenger) otherObj)
                    .mTarget.asBinder());
        } catch (ClassCastException e) {
        }
        return false;
    }

    public int hashCode() {
        return mTarget.asBinder().hashCode();
    }

    /**
     * Create a Messenger from a raw IBinder, which had previously been
     * retrieved with {@link #getBinder}.
     * 
     * @param target The IBinder this Messenger should communicate with. change
     *            the parameter from IBinder to IMessager
     */
    public Messenger(IBinder target) {
        mTarget = IMessenger.Stub.asInterface(target);
    }
    
    public Message obainRTMessage(){
		return null;
    }
}
