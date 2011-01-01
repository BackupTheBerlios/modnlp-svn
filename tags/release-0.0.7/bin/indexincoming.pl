#!/usr/bin/perl
# indexincoming.pl
# Created Mon Jun 21 2010 by S Luz luzs@cs.tcd.ie
# $Id$
# $Log$
 use Cwd;

$IDX_BIN = $ARGV[0];
$dry_run = $ARGV[1];
die "Usage: indexincoming.pl idx_binary_directory [-dry-run]\n"
    unless $IDX_BIN;

$INCOMING_DIR = cwd();


## set these variables to point to your corpus files
$INDEX_DIR = '../index';
$TEXT_DIR = '../text';
$HEADERS_DIR = '../headers';
$HEADERS_URL = "file://$INCOMING_DIR/../headers/";
$DATE = localtime();
$DATE =~ tr/ /_/;

$TEXT_LIST_FILE = "$INCOMING_DIR/indexed_on_$DATE.lst";
@TEXT_LIST=sort(<*.xml>);
@HEADERS_LIST=sort(<*.hed>);


@haux = @HEADERS_LIST;
open(FL, ">$TEXT_LIST_FILE") 
    or die "Couldn't open file list: $!\n";
foreach (@TEXT_LIST){
    my $h = $_;
    $h =~ s/\.xml$/.hed/;
    (unlink($TEXT_LIST_FILE) &&
     die "Incoming headers list doesn't match incoming text file list: $_ != $h.\n")
        unless shift(@haux) eq $h;
        (unlink($TEXT_LIST_FILE) &&
         die "Incoming files exists at destination '$TEXT_DIR/$_'")
        if -e "$TEXT_DIR/$_";
    print  FL "$TEXT_DIR/$_\n";
}
close FL;
(unlink($TEXT_LIST_FILE) &&
 die "Size of incoming headers list doesn't match incoming text file list size.\n")
    if $#haux > 0;
    

my $cmd = 'cp '. join(' ', @TEXT_LIST)." $TEXT_DIR/";
Run($cmd)
    or die "error running: '$cmd': $!\n";
$cmd = 'cp '. join(' ', @HEADERS_LIST)." $HEADERS_DIR/";
if (! Run($cmd)){
    Run("rm $TEXT_DIR/{".join(',', @TEXT_LIST)."}");
    Run("rm $HEADERS_DIR/{".join(',', @HEADERS_LIST)."}");
    unlink($TEXT_LIST_FILE);
    die "error running: '$cmd': $!\n";
}

if (!chdir($IDX_BIN)){
    Run("rm $TEXT_DIR/{".join(',', @TEXT_LIST)."}");
    Run("rm $HEADERS_DIR/{".join(',', @HEADERS_LIST)."}");
    unlink($TEXT_LIST_FILE);
    die "error running: 'chdir($IDX_BIN)': $!\n";
}

print "Now at $IDX_BIN \n";

$cmd = "./runidx.sh $INDEX_DIR $TEXT_LIST_FILE $HEADERS_DIR $HEADERS_URL";
if (! Run($cmd)){
    Run("rm $TEXT_DIR/{".join(',', @TEXT_LIST)."}");
    Run("rm $HEADERS_DIR/{".join(',', @HEADERS_LIST)."}");
    unlink($TEXT_LIST_FILE);
    die "error running: '$cmd': $!\n";
}

chdir($INCOMING_DIR);
unlink @TEXT_LIST;
unlink @HEADERS_LIST;

print 'Files '.join(', ',@TEXT_LIST)." + headers have been indexed and removed from incoming folder.\n";

sub Run {
    my $c = shift;

    print "$c \n";
    return 1
        if $dry_run;
    
    return system($c) == 0 ; 
}
