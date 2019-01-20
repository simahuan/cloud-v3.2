/*
 * Copyright 2009-2011 Jon Stevens et al.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package de.aflx.sardine.impl.handler;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import de.aflx.sardine.impl.SardineException;

/**
 * {@link org.apache.http.client.ResponseHandler} which checks wether a given resource exists.
 *
 * @author mirko
 * @version $Id: ExistsResponseHandler.java 258 2011-05-30 17:50:04Z latchkey $
 */
public class ExistsResponseHandlerExtend extends ValidatingResponseHandler<Boolean>
{
	public Boolean handleResponse(HttpResponse response) throws SardineException
	{
		Header[] headers=response.getHeaders("Allow");
		if(headers.length>0){
			String value=headers[0].getValue().toLowerCase();
			if(value.contains("get")){
				return true;
			}
		}
		return false;
	}
}
