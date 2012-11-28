#!/usr/bin/perl
# -*- CPerl -*-

# handle assignment submission

# Author: Saturnino Luz luzs@acm.org
# Created: 2 Mar 2004
# $Revision: 1.0 $
# Keywords:

#     Permission  to  use,  copy, and distribute is hereby granted,
#     providing that the above copyright notice and this permission
#     appear in all copies and in supporting documentation.

# Commentary:

# Change log:
#
use strict;
my $rcsid =   "\$Id: rn.cgi,v 1.4 2001/04/02 17:03:30 luzs Exp $>";
my $version = substr("\$Revision: 1.4 $>",11,-length($0));

BEGIN{
  # Initialise CGI output
  print STDOUT "Content-type: text/html\n\n";
  use lib "..";
}

use CGI_Lite;


my $db = 0;
my $cgi  = new CGI_Lite;
$cgi->set_directory("/tmp/") || die "Error setting tmp directory";
$cgi->set_file_type('file');
$cgi->add_timestamp(1);
my %data = $cgi->parse_form_data;
my %cookies = $cgi->parse_cookies;

my @files = keys %data;
#my $lname = `pwd`;
#print "\n----$lname\n";
#$lname =~ s|.*/(.+)$|$1|;
#print "\n----$lname\n";
#my $finger = `finger -s $lname`;
#print "\n----$finger\n";
#my ($lname, $uname, $tty, $d, $m) = split(/\t/, $finger);
### read data from file instead
my $idfile = '.id';
open ID, "$idfile" || die "Error reading $idfile $!";
my $lname = <ID>; # get login name
my $uname = <ID>; # get user name
close ID;
chomp $lname;
chomp $uname;

#print "Content-type:text/plain", "\n\n";
#$cgi->print_form_data();

print '<html><body><form action="aling.cgi" method="POST" enctype="multipart/form-data">';
print "\n<h3>$uname"."'s ($lname) assignment submission page</h3>";
print "--S: $data{$files[0]}, T: $data{$files[1]}\n";

if ($data{$files[0]} && -s "/tmp/$data{$files[0]}" && $data{$files[1]} && -s "/tmp/$data{$files[1]}") {
  my $r = rand(100);
  print "S: $data{$files[0]}, T: $data{$files[1]}\n";
  rename("/tmp/$data{$files[1]}", "/tmp/source$r.txt")
    || die "Error moving $data{$files[0]}: $!";
  rename("/tmp/$data{$files[0]}", "/tmp/target$r.txt")
    || die "Error moving $data{$files[1]}: $!";
 #convert windows line endings to unix ones
  `perl dos2unix.pl /tmp/target$r.txt`;
  `perl dos2unix.pl /tmp/source$r.txt`;
  #split sentences
  `perl split-sentences.perl -l es < /tmp/source$r.txt > /tmp/source_s$r.txt`;
  `perl split-sentences.perl -l en < /tmp/target$r.txt > /tmp/target_s$r.txt`;
  print "Running./hunalign -text null.dic source$r.txt target$r.txt ...\n";
  print "<beginalignment>\n";
   my $c =  `./hunalign -text null.dic /tmp/source_s$r.txt /tmp/target_s$r.txt`;
  # my $y = `cp /tmp/source_aligned$r.txt /home/gplynch/public_html/source_aligned.txt`;
  `rm /tmp/source$r.txt`;
  `rm /tmp/target$r.txt`;
  `rm /tmp/source_s$r.txt`;
  `rm /tmp/target_s$r.txt`;
  `rm /tmp/$data{$files[0]}`;
  `rm /tmp/$data{$files[1]}`;
  print $c;
  print "<beginalignment>\n";
}
else {
  print "\n<p>Cannot Find files";
}

print "<br><br>Upload source     <INPUT NAME='source' TYPE='file' size=40><br>";
print "<br><br>Upload target     <INPUT NAME='target' TYPE='file' size=40><br>";
print "<input type='submit' value='Submit'></form></body></html>";
# end
