package org.eclipse.ecf.internal.provider.filetransfer.httpclient45.win32;

import java.util.Map;

import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.BrowseFileTransferException;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.osgi.service.component.annotations.Component;

import org.eclipse.ecf.internal.provider.filetransfer.httpclient45.DefaultNTLMProxyHandler;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient45.INTLMProxyHandler;

@Component(service = { INTLMProxyHandler.class })
public class Win32NTLMProxyHandler extends DefaultNTLMProxyHandler {

	@Override
	public boolean allowNTLMAuthentication(Map<?, ?> connectOptions) {
		DefaultNTLMProxyHandler.setSeenNTLM();
		return true;
	}

	protected boolean isExplicitAllowNTLMAuthentication() {
		return super.allowNTLMAuthentication(null);
	}

	@Override
	public void handleNTLMProxy(Proxy proxy, int code) throws IncomingFileTransferException {
		DefaultNTLMProxyHandler.setSeenNTLM();
		if (Win32HttpClientConfigurationModifier.isWinAuthAvailable()
				&& (code != 407 || isExplicitAllowNTLMAuthentication())) {
			return;
		}
		super.handleNTLMProxy(proxy, code);
	}

	@Override
	public void handleSPNEGOProxy(Proxy proxy, int code) throws BrowseFileTransferException {
		if (Win32HttpClientConfigurationModifier.isWinAuthAvailable()
				&& (code != 407 || isExplicitAllowNTLMAuthentication())) {
			return;
		}
		super.handleSPNEGOProxy(proxy, code);
	}

}
