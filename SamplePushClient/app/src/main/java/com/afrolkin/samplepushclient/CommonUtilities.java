/**
 * Copyright (c) 2015 BlackBerry Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afrolkin.samplepushclient;

public class CommonUtilities {
    // On the BlackBerry Android Runtime, the SENDER_ID can be any value as it is not required for
    // registration with the push service. On any other Android Device, the SENDER_ID must be a
    // valid Sender ID retrieved from Google when enabling Cloud Messaging for your app
    static final String SENDER_ID = "123456789123";
}
