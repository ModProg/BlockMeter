#!/usr/bin/perl

open(IN, 'ack -o "(class|method|field)_[0-9]+" |cut -d: -f3 | sort -u|');
while (<IN>) {
	chomp;
	$cmf=$_;
	open(MAP, "</home/gbl/.gradle/caches/fabric-loom/mappings/yarn-1.15+build.2-v2.tiny");
	while (<MAP>) {
		chomp;
		@f=split();
		if (substr($cmf, 0, 5) eq "class" && $f[2] eq "net/minecraft/$cmf") {
			my $fullclass=$f[3];
			$fullclass=~s|.*/||;
			print "s/$cmf/$fullclass/g\n";
		}

		elsif ($cmf eq $f[3]) {
			print "s/$cmf/$f[4]/g\n";
		}
	}
		close MAP;
}
