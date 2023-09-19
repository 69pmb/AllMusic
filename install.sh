#!/bin/sh

# Define variables
ALIAS_COMMAND="music"
SCRIPT_NAME="AllMusic.sh"
SCRIPT_PATH="$(realpath "bin/$SCRIPT_NAME")"
SCRIPT_DIR="$(dirname "$SCRIPT_PATH")"
RC_FILE=""
SHELL=""

# Check if zsh or bash is used and set RC_FILE accordingly
if [ -f ~/.zshrc ]; then
    RC_FILE="$HOME/.zshrc"
    SHELL="zsh"
elif [ -f ~/.bashrc ]; then
    RC_FILE="$HOME/.bashrc"
    SHELL="bash"
else
    echo "Unsupported shell"
    exit 1
fi

if alias | grep $ALIAS_COMMAND; then
    echo "The alias '$ALIAS_COMMAND' already exists in the configuration file."
    exit 1
fi

echo "alias $ALIAS_COMMAND='bash $SCRIPT_PATH'" >>$RC_FILE

echo "'$SCRIPT_NAME' has been installed, you can use it by entering '$ALIAS_COMMAND'"

sh -c "$SHELL"
