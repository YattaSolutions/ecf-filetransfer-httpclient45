/*******************************************************************************
* Copyright (c) 2019 Yatta Solutions and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Yatta Solutions - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient45;

import java.util.Map;
import org.apache.http.HttpStatus;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.BrowseFileTransferException;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.provider.filetransfer.httpclient45.HttpClientOptions;

public class DefaultNTLMProxyHandler implements INTLMProxyHandler {
	private static boolean seenNTLM;

	public static boolean seenNTLM() {
		return seenNTLM;
	}

	public static void setSeenNTLM() {
		seenNTLM = true;
	}

	public static void resetSeenNTLM() {
		seenNTLM = false;
	}

	@Override
	public boolean allowNTLMAuthentication(Map<?, ?> connectOptions) {
		seenNTLM = true;
		if (connectOptions != null && connectOptions.get(HttpClientOptions.FORCE_NTLM_PROP) != null) {
			return true;
		}
		return (System.getProperty(HttpClientOptions.FORCE_NTLM_PROP) != null);
	}

	@Override
	public void handleNTLMProxy(Proxy proxy, int code) throws IncomingFileTransferException {
		seenNTLM = true;
		if (code >= HttpStatus.SC_BAD_REQUEST) {
			throw new IncomingFileTransferException("HttpClient Provider is not configured to support NTLM proxy authentication.", //$NON-NLS-1$
					HttpClientOptions.NTLM_PROXY_RESPONSE_CODE);
		}
	}

	@Override
	public void handleSPNEGOProxy(Proxy proxy, int code) throws BrowseFileTransferException {
		if (code >= HttpStatus.SC_BAD_REQUEST) {
			throw new BrowseFileTransferException("HttpClient Provider does not support the use of SPNEGO proxy authentication."); //$NON-NLS-1$
		}
	}

}
