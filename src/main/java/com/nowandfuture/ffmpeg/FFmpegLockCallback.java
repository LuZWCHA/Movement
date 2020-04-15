package com.nowandfuture.ffmpeg;

/*
 * Copyright (C) 2009-2019 Samuel Audet
 *
 * Licensed either under the Apache License, Version 2.0, or (at your option)
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation (subject to the "Classpath" exception),
 * either version 2, or any later version (collectively, the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     http://www.gnu.org/licenses/
 *     http://www.gnu.org/software/classpath/license.html
 *
 * or as provided in the LICENSE.txt file that accompanied this code.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Based on the avcodec_sample.0.5.0.c file available at
 * http://web.me.com/dhoerl/Home/Tech_Blog/Entries/2009/1/22_Revised_avcodec_sample.c_files/avcodec_sample.0.5.0.c
 * by Martin Böhme, Stephen Dranger, and David Hoerl
 * as well as on the decoding_encoding.c file included in FFmpeg 0.11.1,
 * which is covered by the following copyright notice:
 *
 * Copyright (c) 2001 Fabrice Bellard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.bytedeco.ffmpeg.avcodec.Cb_PointerPointer_int;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.annotation.Cast;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.bytedeco.ffmpeg.global.avcodec.*;

public class FFmpegLockCallback {
    private static boolean initialized = false;

    private static AtomicInteger lockCounter = new AtomicInteger(0);
    private static HashMap<Integer, Lock> lockArray = new HashMap<>();
    private static Cb_PointerPointer_int lockCallback = new Cb_PointerPointer_int() {
        @Override
        public int call(@SuppressWarnings("rawtypes") @Cast("void**") PointerPointer mutex, @Cast("AVLockOp") int op) {
            int number;
            Lock l;
            // System.out.println "Locking: " + op);
            switch (op) {
                case AV_LOCK_CREATE:
                    number = lockCounter.incrementAndGet();
                    // System.out.println("Command: " + op + " number: " + number);
                    new IntPointer(mutex).put(0, number);
                    lockArray.put(number, new ReentrantLock());
                    return 0;
                case AV_LOCK_OBTAIN:
                    number = new IntPointer(mutex).get(0);
                    // System.out.println("Command: " + op + " number: " + number);
                    l = lockArray.get(number);
                    if (l == null) {
                        System.err.println("Lock not found!");
                        return -1;
                    }
                    l.lock();
                    return 0;
                case AV_LOCK_RELEASE:
                    number = new IntPointer(mutex).get(0);
                    // System.out.println("Command: " + op + " number: " + number);
                    l = lockArray.get(number);
                    if (l == null) {
                        System.err.println("Lock not found!");
                        return -1;
                    }
                    l.unlock();
                    return 0;
                case AV_LOCK_DESTROY:
                    number = new IntPointer(mutex).get(0);
                    // System.out.println("Command: " + op + " number: " + number);
                    lockArray.remove(number);
                    mutex.put(0, null);
                    return 0;
                default:
                    return -1;
            }
        }
    }.retainReference();

    public static synchronized void init() {
        if (!initialized) {
            initialized = true;
            av_lockmgr_register(lockCallback);
        }
    }
}
