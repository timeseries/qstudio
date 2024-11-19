/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2023 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.timestored.plugins;

/**
 * Interface to allow retrieving connection details from somewhere external to qStudio. 
 */
public interface DatabaseAuthenticationService {

	/**
	 * @param connectionDetails The connection details as stored in qStudio.
	 * @return Whatever you return will be the details used for connecting to kdb.
	 */
	public ConnectionDetails getonConnectionDetails(ConnectionDetails connectionDetails);

	/** @return Descriptive name for this authentication mechanism **/
	public String getName();
}
