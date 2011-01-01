#!/usr/bin/perl
# deindex.pl
# Created Mon Jun 21 2010 by S Luz luzs@cs.tcd.ie
# $Id$
# $Log$
use Cwd;
use File::Basename;

$IDX_BIN = shift @ARGV;
$dry_run = 0;
@TEXT_LIST = @ARGV;

die "Usage: indexincoming.pl idx_binary_directory\n"
    unless $IDX_BIN;

## set these variables to point to your corpus files
$INDEX_DIR = '/home/luzs/corpora/indices/tec';
$TEXT_DIR = '/home/luzs/corpora/tec/text';
$HEADERS_DIR = '/home/luzs/corpora/tec/headers';
@HEADERS_LIST=();
$HEADERS_URL = 'http://ronaldo.cs.tcd.ie/tec2/headers/';
$DATE = localtime();
$DATE =~ tr/ /_/;

$INCOMING_DIR = cwd();
$TEXT_LIST_FILE = "$INCOMING_DIR/deindexed_on_$DATE.lst";

open(FL, ">$TEXT_LIST_FILE") 
    or die "Couldn't open file list $TEXT_LIST_FILE: $!\n";
foreach (@TEXT_LIST){
    print  FL "$_\n";
    push(@HEADERS_LIST,$HEADERS_DIR.'/'.basename($_,(".xml",".hed")).'.hed');
}
close FL;

if (!chdir($IDX_BIN)){
    die "error running: 'chdir($IDX_BIN)': $!\n";
}

print "Now at $IDX_BIN \n";

$cmd = "./runidx.sh $INDEX_DIR $TEXT_LIST_FILE $HEADERS_DIR $HEADERS_URL -v -d";
if (! Run($cmd)){
    die "error running: '$cmd': $!\n";
}

unlink @TEXT_LIST
    unless $dry_run;
unlink @HEADERS_LIST
    unless $dry_run;

print 'Files '.join(', ',@TEXT_LIST)." + headers have been de-indexed and removed from corpus.\n";

sub Run {
    my $c = shift;

    print "$c \n";
    return 1
        if $dry_run;
    
    return system($c) == 0 ; 
}
