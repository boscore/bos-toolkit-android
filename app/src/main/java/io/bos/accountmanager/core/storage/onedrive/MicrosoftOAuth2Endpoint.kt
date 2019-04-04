
// ------------------------------------------------------------------------------
//  Copyright (c) Microsoft Corporation.  All Rights Reserved.  Licensed under the MIT License.  See License in the project root for license information.
// ------------------------------------------------------------------------------

package io.bos.accountmanager.core.storage.onedrive

import android.net.Uri

import com.microsoft.services.msa.OAuthConfig

/**
 * The configuration for an Microsoft OAuth2 v2.0 Endpoint
 */
internal class MicrosoftOAuth2Endpoint : OAuthConfig {

    override fun getAuthorizeUri(): Uri {
        return Uri.parse("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
    }

    override fun getDesktopUri(): Uri {
        return Uri.parse("urn:ietf:wg:oauth:2.0:oob")
    }

    override fun getLogoutUri(): Uri {
        return Uri.parse("https://login.microsoftonline.com/common/oauth2/v2.0/logout")
    }

    override fun getTokenUri(): Uri {
        return Uri.parse("https://login.microsoftonline.com/common/oauth2/v2.0/token")
    }

    companion object {

        /**
         * The current instance of this class
         */
        /**
         * The current instance of this class
         * @return The instance
         */
        val instance = MicrosoftOAuth2Endpoint()
    }
}