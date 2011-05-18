<?php
/*
 * index.php
 *
 * Copyright (c) 2006-2008 Operational Dynamics Consulting Pty Ltd
 * 
 * This file comprises part of the infrastructure and content of the
 * java-gnome project website. As such, it is conveyed alongside the source
 * code and is made available to you by its authors under the terms of the
 * "GNU General Public Licence, version 2". See the LICENCE file for the terms
 * governing usage, copying and redistribution.
 */

	require "template.inc";
?>
<html>
<head>
<?
	template_header();
?>
<title>Design, Architecture, and Documentation for java-gnome 4.0</title>
<meta name="author" content="Andrew Cowie">
<style>
a.directory {
	font-family: "Bitstream Vera Sans Mono", "Courier New", mono;
	text-decoration: none;
}
</style>
</head>
<body>
<?
	template_begin();
?>
<h1 class="title">Documentation</h1>

<div class="bluepanel">
	The <a class="nav-white" href="/README.html"><code>README</code></a>,
	<a class="nav-white" href="/HACKING.html"><code>HACKING</code></a>, and
	<a class="nav-white" href="/NEWS.html"><code>NEWS</code></a> files;
	<a class="nav-white" href="/doc/api/4.1/overview-summary.html">API
	documentation</a>;
	<a class="nav-white" href="/doc/design/START.html">Design and
	architecture</a>
</div>


<h1>Getting Started</h1>

<h2><a class="directory" href="/README.html">README</a></h2>

<p>The <a href="/README.html"><code>README</code></a> file in the top level
directory of the source code contains everything you need to get started: how
to get the code in the first place, how to configure and build it, and how to
use java-gnome as a library in your projects.</p>

<h2><a class="directory" href="examples/START.html">doc/examples/</a></h2>

<p>While not a complete tutorials in themselves, we have a growing number
of small example applications that ship with the source, including one 
for the complex TreeView / TreeModel API. See 
<a href="/doc/examples/START.html"><code>doc/examples/START</code></a>
for links to those that have been formatted for web viewing, but remember all
the source code is always viewable online if you don't have a checkout handy.
Just pull up the

'<a href="http://research.operationaldynamics.com/bzr/java-gnome/mainline/" style="text-decoration: none;"><code>mainline</code></a>'

branch and navigate to 

<a href="http://research.operationaldynamics.com/bzr/java-gnome/mainline/doc/examples/" style="text-decoration: none;"><code>doc/examples/</code></a>. The same goes for the rest
of the java-gnome code base; it's all there, including the sources for this website.
</p>


<h2>Tutorials</h2>

<p><i>Tutorials to get people started and guides about how to use the more
complex APIs are still a work in progress. They will start appearing on this
website in the coming months.</i></p>

<h1>API Documentation</h1>

<h2><a class="directory" href="api/4.1/overview-summary.html">doc/api/</a></h2>

<p>We have put considerable effort into ensuring the java-gnome bindings are
easily usable for people using IDEs like Eclipse, and a big part of that is
having useful JavaDoc so that the help popups you get when you hover over a
class or method helps to the programmer figure out what they need to do in
that context.</p>

<p>The API documentation in the underlying GNOME libraries tends to be sparse
and is very much in the reference style. That's great if you already know what
you're doing and just need to look something up, but isn't much help if you
are trying to figure out how to use a library in the first place. Because of
our focus on approachability, we take a somewhat different approach. The API
documentation in java-gnome is in a more conversational style and is squarely
aimed at helping people learn GTK. Where possible we also include code
snippets to help you understand <i>how</i> to use the APIs,</p> 

<p>The API documentation is of course generated by <code>javadoc</code> when
you build the source code and is located at <code>doc/api/4.1/</code>. The
canonical public version of the <a href="api/4.1/">JavaDoc</a> is hosted here; you
can link to it from your own JavaDoc if you like.

<h1>Contributing</h1>

<p>java-gnome is an Open Source project, and as such is the sum of the
contributions of the hackers who actually write the bindings code, the
maintainers who take care of library's build system and do releases, the
packagers downstream who make the software available in their distributions,
and of course you, the developers using the library to write your
applications.  who send in bug reports and feedback.</p>

<h2><a class="directory" href="/HACKING.html">HACKING</a></h2>

<p>Providing complete coverage of the underlying GTK and GNOME libraries is a
mammoth task and you are welcome to get involved!</p>

<p>java-gnome has been carefully designed to be extensible, maintainable,
and above all easy to hack on. Like any large project, though, there are
complexities and common practises that you will want to be on top of before
trying to work on it yourself.

<p>Anyone wishing to contribute to the java-gnome project is therefore advised
to read the top level <a href="/HACKING.html"><code>HACKING</code></a> file
before doing anything else. It provides instructions on how to checkout the
source code in such a way that you can easily work on your own branches, tips
on how to go about learning the internals of the bindings, and links to the
style guide.</p>


<h2><a class="directory" href="design/">doc/design/</a></h2>

<p>The source code contains considerable reference material about the
background of the project, the design constraints which governed the
re-engineering effort and discussion of the architecture we arrived at. Much
of this documentation is made available here on this website, cunningly
rendered into HTML form.</p>

<p>A good starting point is the <a
href="/doc/design/START.html"><code>doc/design/START</code></a> file, which
links to each of the other documents in that directory.

<h2><a class="directory" href="style/">doc/style/</a></h2>

<p>In order to meet our quality and approachability goals, we have put in
place certain guidelines to help keep us co-ordinated and to minimize
friction. If you'd like to ensure that code or documentation you might choose
to send in will be accepted, please read the documents in the
<code>doc/style/</code> directory carefully. They are in essence an extension
of the <code>HACKING</code> file and together with the design documentation
tell you everything you need to know to be able to contribute to java-gnome.

<?
	template_end();
?>
</body>
</html>

<!-- vim: set textwidth=78: -->
