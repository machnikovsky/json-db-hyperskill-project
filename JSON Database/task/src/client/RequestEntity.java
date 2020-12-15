package client;

public class RequestEntity {

        public String type;
        public String key;
        public String value;
        public String file;

        public RequestEntity() {
        }

        public void makeRequestFromArgs(Args args) {
                this.type = args.type;
                this.key = args.key;
                this.value = args.value;
                this.file = args.value;
        }

}
