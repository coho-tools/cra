#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/fcntl.h>
#include <sys/resource.h>
//#include <sys/unistd.h>
#include <unistd.h>
#include <errno.h>
#include <sys/wait.h>

/* This c function fork a process to execute a command */

#define BUFFERSIZE 1024

static void usage(){
	fprintf(stderr,
		"usage: fork [-c cmd ] \n  \
		cmd: the command to execute.\n"); 
	exit(1);
}

int main(int argc, char **argv) {
	/*get the cmd from input*/
	char *cmd = ""; int c; 
	while ((c = getopt(argc, argv, "c:"))!=EOF) {
		switch(c){
		case 'c':
			cmd = optarg;
			if(strlen(cmd)>BUFFERSIZE){
				fprintf(stderr, "The length of CLASSPATH must be less than %d character", BUFFERSIZE);
				usage();
			}
			break;
		default:
			usage();
		}
	}
	if (optind < argc) {
		usage();
	}

//  Mark said there is a bug in csh. 
//  Comment out the following code if the bug is reproduced.
//	int i, ndesc;
//	struct rlimit r;
//	FILE *f;
//	/* csh has a bug, we'll close all of our files before we fork */
//	if(getrlimit(RLIMIT_NOFILE, &r)) ndesc = 32;
//	else ndesc = r.rlim_max;
//	for(i = 0; i < ndesc; i++)
//		close(i);
//
//	/* now, re-open /dev/tty as stderr */
//	i = open("/dev/tty", O_WRONLY);
//	if(i >= 0) {
//		if(dup2(i, 2) < 0) {
//			f = fdopen(i, "w");
//			if(f != NULL) {
//				fprintf(f, "%s: dup2 failed\n", argv[0]);
//				perror("  ");
//				exit(1);
//			}
//		}
//		close(i);
//	}

	/* no stdout and stdin */

	/* now fork */
	int cpid;
	if((cpid = fork())<0){
		fprintf(stderr, "%s: fork failed\n", argv[0]);
		exit(1);
	}else if(cpid==0){
		/* execute the java stuff */
		int status;
		status = system(cmd); 
		if(status) { 
			fprintf(stderr, "failed command -- %s\n status %d\n errno=%d\n", cmd,WEXITSTATUS(status),errno);
		}
		exit(status);
	}else{
		exit(0);
	}
}
