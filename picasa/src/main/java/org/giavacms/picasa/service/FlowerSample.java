/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.giavacms.picasa.service;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetAccessToken;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetTemporaryToken;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.xml.atom.AtomParser;
import com.google.api.data.sample.picasa.model.AlbumEntry;
import com.google.api.data.sample.picasa.model.AlbumFeed;
import com.google.api.data.sample.picasa.model.PhotoEntry;
import com.google.api.data.sample.picasa.model.PicasaUrl;
import com.google.api.data.sample.picasa.model.UserFeed;
import com.google.api.data.sample.picasa.model.util.Util;

/**
 * @author Yaniv Inbar
 */
public class FlowerSample {

	enum AuthType {
		OAUTH, CLIENT_LOGIN
	}

	private static AuthType AUTH_TYPE = AuthType.CLIENT_LOGIN;
	static OAuthHmacSigner signer;
	static OAuthCredentialsResponse credentials;
	private static String USERNAME = "fiorenzino@gmail.com";
	private static String PWD = "d10b01a";

	public static void main(String[] args) throws IOException {
		Util.enableLogging();
		try {
			HttpTransport transport = setUpTransport();
			// UserFeed feed = showAlbums(transport, 100, false);

			String feedLink = "http://picasaweb.google.com/data/feed/api/user/110486863202316926319/albumid/5721834475869578497?authkey=Gv1sRgCPeT0O-9vommLA";
			showPhotoByAlbumFeedLink(transport, feedLink);
		} catch (HttpResponseException e) {
			System.err.println(e.response.parseAsString());
			throw e;
		} finally {
			if (credentials != null) {
				try {
					GoogleOAuthGetAccessToken
							.revokeAccessToken(createOAuthParameters());
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	private static HttpTransport setUpTransport()
			throws IOException {
		HttpTransport transport = GoogleTransport.create();
		GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("google-picasaatomsample-1.0");
		headers.gdataVersion = "2";
		AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
		transport.addParser(parser);
		if (AUTH_TYPE == AuthType.OAUTH) {
			authorizeUsingOAuth(transport);
		} else {
			authorizeUsingClientLogin(transport);
		}
		return transport;
	}

	private static OAuthParameters createOAuthParameters() {
		OAuthParameters authorizer = new OAuthParameters();
		authorizer.consumerKey = "anonymous";
		authorizer.signer = signer;
		authorizer.token = credentials.token;
		return authorizer;
	}

	private static void authorizeUsingOAuth(HttpTransport transport)
			throws IOException {
		GoogleOAuthGetTemporaryToken temporaryToken = new GoogleOAuthGetTemporaryToken();
		signer = new OAuthHmacSigner();
		signer.clientSharedSecret = "anonymous";
		temporaryToken.signer = signer;
		temporaryToken.consumerKey = "anonymous";
		temporaryToken.scope = "http://picasaweb.google.com/data";
		temporaryToken.displayName = "Picasa Atom XML Sample for the GData Java library";
		OAuthCredentialsResponse tempCredentials = temporaryToken.execute();
		signer.tokenSharedSecret = tempCredentials.tokenSecret;
		System.out
				.println("Please go open this web page in a browser to authorize:");
		GoogleOAuthAuthorizeTemporaryTokenUrl authorizeUrl = new GoogleOAuthAuthorizeTemporaryTokenUrl();
		authorizeUrl.temporaryToken = tempCredentials.token;
		System.out.println(authorizeUrl.build());
		System.out.println();
		System.out.println("Press enter to continue...");
		new Scanner(System.in).nextLine();
		GoogleOAuthGetAccessToken accessToken = new GoogleOAuthGetAccessToken();
		accessToken.temporaryToken = tempCredentials.token;
		accessToken.signer = signer;
		accessToken.consumerKey = "anonymous";
		accessToken.verifier = "";
		credentials = accessToken.execute();
		signer.tokenSharedSecret = credentials.tokenSecret;
		createOAuthParameters().signRequestsUsingAuthorizationHeader(transport);
	}

	private static void authorizeUsingClientLogin(HttpTransport transport)
			throws IOException {
		ClientLogin authenticator = new ClientLogin();
		authenticator.authTokenType = "lh2";
		authenticator.username = USERNAME;
		authenticator.password = PWD;
		authenticator.authenticate().setAuthorizationHeader(transport);
	}

	private static UserFeed showAlbums(HttpTransport transport, int maxResult,
			boolean showPhoto) throws IOException {
		// build URL for the default user feed of albums
		PicasaUrl url = PicasaUrl.relativeToRoot("feed/api/user/default");
		// execute GData request for the feed
		UserFeed feed = UserFeed.executeGet(transport, url, maxResult);
		System.out.println("User: " + feed.author.name);
		System.out.println("Total number of albums: " + feed.totalResults);
		// show albums
		if (feed.albums != null) {
			for (AlbumEntry album : feed.albums) {
				showAlbum(transport, album, showPhoto);
			}
		}
		return feed;
	}

	private static void showAlbum(HttpTransport transport, AlbumEntry album,
			boolean showPhoto) throws IOException {
		System.out.println();
		System.out.println("-----------------------------------------------");
		System.out.println("Album title: " + album.title);
		System.out.println("Updated: " + album.updated);
		System.out.println("Album ETag: " + album.etag);
		System.out.println("Album url: " + album.getFeedLink());
		if (album.summary != null) {
			System.out.println("Description: " + album.summary);
		}
		if (showPhoto && album.numPhotos != 0) {
			System.out.println("Total number of photos: " + album.numPhotos);
			System.out.println("feddlink: " + album.getFeedLink());
			showPhotoByAlbumFeedLink(transport, album.getFeedLink());
		}
	}

	private static void showPhotoByAlbumFeedLink(HttpTransport transport,
			String feedLink) throws IOException {
		PicasaUrl url = new PicasaUrl(feedLink);
		AlbumFeed feed = AlbumFeed.executeGet(transport, url, 100);
		for (PhotoEntry photo : feed.photos) {
			// System.out.println();
			// System.out.println("Photo title: " + photo.title);
			if (photo.summary != null) {
				// System.out.println("Photo description: " + photo.summary);
			}
			// System.out.println("Image URL: " + photo.mediaGroup.content.url);
			// System.out.println("w/h: " + photo.mediaGroup.content.width + "/"
			// + photo.mediaGroup.content.height);
			// System.out.println("thumbnail.w/h: "
			// + photo.mediaGroup.thumbnail.width + "/"
			// + photo.mediaGroup.thumbnail.height);
			// System.out.println("thumbnail.url: "
			// + photo.mediaGroup.thumbnail.url);
			System.out.println("{filename:'" + photo.title + "',width:"
					+ photo.mediaGroup.content.width + ",height:"
					+ photo.mediaGroup.content.height + ",caption:\""
					+ photo.title + "\",thumbnail:'"
					+ photo.mediaGroup.thumbnail.url + "'," + "large:'"
					+ photo.mediaGroup.content.url + "'},");
		}
	}

	private static AlbumEntry postAlbum(HttpTransport transport, UserFeed feed)
			throws IOException {
		System.out.println();
		AlbumEntry newAlbum = new AlbumEntry();
		newAlbum.access = "private";
		newAlbum.title = "A new album";
		newAlbum.summary = "My favorite photos";
		AlbumEntry album = feed.insertAlbum(transport, newAlbum);
		showAlbum(transport, album, true);
		return album;
	}

	private static PhotoEntry postPhoto(HttpTransport transport,
			AlbumEntry album) throws IOException {
		String fileName = "picasaweblogo-en_US.gif";
		String photoUrlString = "http://www.google.com/accounts/lh2/"
				+ fileName;
		URL photoUrl = new URL(photoUrlString);
		PhotoEntry photo = PhotoEntry.executeInsert(transport,
				album.getFeedLink(), photoUrl, fileName);
		System.out.println("Posted photo: " + photo.title);
		return photo;
	}

	private static AlbumEntry getUpdatedAlbum(HttpTransport transport,
			AlbumEntry album) throws IOException {
		album = AlbumEntry.executeGet(transport, album.getSelfLink());
		showAlbum(transport, album, true);
		return album;
	}

	private static AlbumEntry updateTitle(HttpTransport transport,
			AlbumEntry album) throws IOException {
		AlbumEntry patched = album.clone();
		patched.title = "My favorite web logos";
		album = patched.executePatchRelativeToOriginal(transport, album);
		showAlbum(transport, album, true);
		return album;
	}

	private static void deleteAlbum(HttpTransport transport, AlbumEntry album)
			throws IOException {
		album.executeDelete(transport);
		System.out.println();
		System.out.println("Album deleted.");
	}
}
