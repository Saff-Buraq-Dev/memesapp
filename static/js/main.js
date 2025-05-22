const { useQuasar } = Quasar;
const { ref } = Vue;

let passwordRules = {
    lower: { text: 'Le mot de passe doit contenir au moins une lettre minuscule.', valid: false },
    upper: { text: 'Le mot de passe doit contenir au moins une lettre majuscule.', valid: false },
    digit: { text: 'Le mot de passe doit contenir au moins un chiffre.', valid: false },
    special: { text: 'Le mot de passe doit contenir au moins un caractère spécial.', valid: false },
    length: { text: 'Le mot de passe doit contenir au moins 8 caractères.', valid: false }
};

const USERS_URL = '/api/users';
const LOGIN_CHECK_URL = '/api/check_login';

// Sert à initialiser l'application Quasar
const app = Vue.createApp({
    delimiters: ['[[', ']]'],
    data() {
        return {
            logged_in: false,
            user: { username: '' },
            login: { name: '', password: '', showPassword: '', btnColor: 'primary', loading: false },
            signup: { name: '', email: '', password: '', confirmation: '', showPassword: false, rules: passwordRules, loading: false, btnColor: 'primary' },
            filesToUpload: [],
            dialog: false,
            accueil: {
                loading: true,
                items: [],
                page: 1,
                per_page: 10,
                total: 0,
                total_pages: 0
            },
            currentPage: 1,
            myFiles: { loading: true, items: [] },
            expanded: false,
        }
    },
    mounted() {
        this.checkLogin();
        this.initItems();
        this.getMyFiles();
    },
    watch: {
        dialog(newVal) {
            if (!newVal) {
                this.clearFiles();
            }
        }
    },
    methods: {
        initItems(page = 1, per_page = 10) {
            this.accueil.loading = true;
            fetch(`/api/files?page=${page}&per_page=${per_page}`)
                .then(response => response.json())
                .then(data => {
                    this.accueil.items = data.files;
                    this.accueil.page = data.page || 1;
                    this.accueil.per_page = data.per_page || 10;
                    this.accueil.total = data.total || 0;
                    this.accueil.total_pages = data.total_pages || 1;
                    this.accueil.loading = false;
                }).catch(error => console.error('Error fetching items:', error));
        },
        getMyFiles(userId) {
            fetch(`/api/myfiles/${userId}`)
                .then(response => response.json())
                .then(data => {
                    this.myFiles.items = data.files;
                    this.myFiles.loading = false;
                    console.log(this.myFiles);
                }).catch(error => console.error('Error fetching my files:', error));
        },
        toggleLike(itemId) {
            // Determine the current like state
            console.log(itemId);
            console.log(this.accueil.items);
            const item = this.accueil.items.find(item => item.id === itemId);
            const userId = this.user.id;
            const isVoted = item.voters.includes(userId);
            const url = isVoted
                ? `/api/unlike/${userId}/${itemId}`
                : `/api/like/${userId}/${itemId}`;

            fetch(url, { method: 'PUT' })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(errorData => {
                            throw new Error(errorData.message || 'An error occurred');
                        });
                    }
                    return response.json();
                })
                .then(() => {
                    if (isVoted) {
                        // Remove the user from the voters array
                        item.voters = item.voters.filter(voter => voter !== userId);
                    } else {
                        // Add the user to the voters array
                        item.voters.push(userId);
                    }
                    this.notifInfo(!isVoted ? 'Liked!' : 'Unliked!');
                })
                .catch(error => {
                    this.notifError('An error occurred while processing your request');
                    console.error(error);
                });
        },
        onPageChange(newPage) {
            this.initItems(newPage, this.accueil.per_page);
        },
        onFilesAdded(files) {
            files.forEach(file => {
                this.filesToUpload.push({
                    filename: file.name,
                    category: '',
                })
            });
        },
        onFilesRemoved(removedFiles) {
            this.filesToUpload = this.filesToUpload.filter(file =>
                !removedFiles.some(removedFile => removedFile.name === file.filename)
            );
        },
        clearFiles() {
            this.filesToUpload = [];
        },
        checkLogin() {
            fetch(LOGIN_CHECK_URL)
                .then(response => response.json())
                .then(data => {
                    this.logged_in = data.logged_in;
                    if (this.logged_in) {
                        this.user = data.user;
                    }
                }).catch(error => this.notifError('Erreur avec le serveur ! Veuillez réessayer plus tard'));
        },
        successUpload() {
            this.notifInfo('La photo a bien été sauvegardée');
            this.checkLogin();
        },
        checkPasswordRules(val) {
            // Define patterns for each criteria
            var lowercasePattern = new RegExp("^(?=.*[a-z])");
            var uppercasePattern = new RegExp("^(?=.*[A-Z])");
            var digitPattern = new RegExp("^(?=.*[0-9])");
            var specialCharPattern = new RegExp("^(?=.*[!@#\$%\^&\*.])");
            var lengthPattern = new RegExp("^(?=.{8,})");

            // Check if the password matches each pattern
            if (!lowercasePattern.test(val)) {
                this.signup.rules.lower.valid = false;
                allOk = false;
            } else {
                this.signup.rules.lower.valid = true;
            }
            if (!uppercasePattern.test(val)) {
                this.signup.rules.upper.valid = false;
                allOk = false;
            } else {
                this.signup.rules.upper.valid = true;
            }
            if (!digitPattern.test(val)) {
                this.signup.rules.digit.valid = false;
                allOk = false;
            } else {
                this.signup.rules.digit.valid = true;
            }
            if (!specialCharPattern.test(val)) {
                this.signup.rules.special.valid = false;
                allOk = false;
            } else {
                this.signup.rules.special.valid = true;
            }
            if (!lengthPattern.test(val)) {
                this.signup.rules.length.valid = false;
                allOk = false;
            } else {
                this.signup.rules.length.valid = true;
            }
        },
        addUser() {
            // Validate form inputs
            if (!(this.$refs.username.validate() && this.$refs.email.validate() && this.$refs.password.validate() && this.$refs.confirmation.validate())) {
                this.notifWarn("Impossible de soumettre le formulaire. Corrigez les erreurs et recommencez !");
                return;
            }

            this.signup.loading = true;

            // Prepare the user data for sending
            const user = {
                username: this.signup.name,
                email: this.signup.email,
                password: this.signup.password
            };

            fetch(USERS_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(user)
            })
                .then(response => {
                    this.signup.loading = false;
                    if (response.ok) {
                        // If the response status is 201 or other successful status
                        this.signup.btnColor = 'primary';
                        this.notifInfo("L'utilisateur a bien été enregistré");
                    } else if (response.status === 409) {
                        // Handle conflict error (e.g., user already exists)
                        return response.json().then(data => {
                            this.signup.btnColor = 'negative';
                            this.notifError(data.error);
                        });
                    } else {
                        // Handle other server errors
                        this.signup.btnColor = 'negative';
                        this.notifError('Erreur avec le serveur');
                    }
                })
                .catch(error => {
                    // Handle network errors
                    this.signup.loading = false;
                    this.signup.btnColor = 'negative';
                    this.notifError('Erreur avec le serveur');
                });
        },
        updateUser() {
            const user = {
                id: this.user.id,
                username: this.user.username,
                email: this.user.email,
            };
            fetch(USERS_URL, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(user)
            })
                .then(response => {
                    if (response.ok) {
                        this.notifInfo("Les modifications ont bien été enregistrées");
                    } else if (response.status === 409) {
                        return response.json().then(data => {
                            this.notifError(data.error);
                        });
                    } else {
                        this.notifError('Erreur avec le serveur');
                    }
                })
                .catch(error => {
                    this.notifError('Erreur avec le serveur');
                });
        }
    },

    setup() {
        const $q = useQuasar();
        const leftDrawerOpen = ref(false)
        return {
            notifInfo(msg) { $q.notify({ message: msg, icon: 'info', color: 'teal', position: 'bottom' }) },
            notifWarn(msg) { $q.notify({ message: msg, icon: 'warning', color: 'orange', position: 'bottom' }) },
            notifError(msg) { $q.notify({ message: msg, icon: 'error', color: 'red', position: 'bottom' }) },
            splitterModel: ref(50),
            model: ref(null),
            modif: ref(false),
            leftDrawerOpen,
            toggleLeftDrawer() {
                leftDrawerOpen.value = !leftDrawerOpen.value
            },
        }
    },

})

app.use(Quasar, {})
Quasar.iconSet.set(Quasar.iconSet.fontawesomeV6)
app.mount('#q-app')