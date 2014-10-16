package server;

/**
 * Represents the request commands that this webbserver can handle
 * @author Mattis
 *
 */
public enum RequestCommands {
	// GET09 = an GET made with HTTP/0.9 protocol, GET10 = an Get-request made with a protocol after 0.9
	GET09, GET10, HEAD10, QUIT; // No headers in 0.9 that's why there is only HEAD10
}
