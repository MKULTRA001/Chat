let naclInstance;
async function saveKeyToLocalStorage(key, keyName) {
    console.log(`Saving ${keyName} to local storage...`);
    try {
        if (key instanceof CryptoKey) {
            const exportedKey = await window.crypto.subtle.exportKey("jwk", key);
            localStorage.setItem(keyName, JSON.stringify(exportedKey));
        } else {
            localStorage.setItem(keyName, JSON.stringify(Array.from(key)));
        }
        console.log(`${keyName} saved to local storage.`);
    } catch (error) {
        console.error(`Error saving ${keyName} to local storage:`, error);
    }
}

async function generateCurve25519KeyPairFromSeed(naclInstance, seedBuffer) {
    // Hash the seed to ensure a consistent 32-byte size
    const hashedSeed = new Uint8Array(await crypto.subtle.digest('SHA-256', seedBuffer));
    const curveKeyPair = naclInstance.crypto_box_seed_keypair(hashedSeed);
    const privateKey = curveKeyPair.boxSk;
    const publicKey = curveKeyPair.boxPk;

    return {
        privateKey,
        publicKey
    };
}

async function handleLogin() {
    try {
        await new Promise((resolve) => {
            nacl_factory.instantiate((instance) => {
                naclInstance = instance;
                resolve();
            });
        });
        console.log('Handling login...');
        console.log('Retrieving username and password...');
        const username = document.querySelector('input[name="username"]').value;
        const password = document.querySelector('input[name="password"]').value;
        const seed = 'key1';

        console.log(`Retrieved username: ${username}`);
        console.log(`Retrieved password: ${password}`);
        console.log('Importing key material...');
        const keyMaterial = await window.crypto.subtle.importKey(
            "raw",
            new TextEncoder().encode(password),
            "PBKDF2",
            false,
            ["deriveBits", "deriveKey"]
        );
        console.log('Imported key material.');

        console.log('Deriving key A...');
        const keyA = await window.crypto.subtle.deriveKey(
            {
                "name": "PBKDF2",
                "salt": new TextEncoder().encode(username + seed),
                "iterations": 100000,
                "hash": "SHA-256"
            },
            keyMaterial,
            {"name": "AES-GCM", "length": 256},
            true,
            ["encrypt", "decrypt"]
        );
        console.log('Derived key A.');
        console.log('Deriving key B...');
        const keyB = await window.crypto.subtle.deriveKey(
            {
                "name": "PBKDF2",
                "salt": new TextEncoder().encode(username + seed + "keyB"),
                "iterations": 100000,
                "hash": "SHA-256"
            },
            keyMaterial,
            {"name": "AES-GCM", "length": 256},
            true,
            ["encrypt", "decrypt"]
        );
        console.log('Derived key B.');

        console.log('Generating Curve25519 key pair...');
        const seedBuffer = new TextEncoder().encode(seed);
        const curveKeyPair = await generateCurve25519KeyPairFromSeed(naclInstance, seedBuffer);
        const privateKey = curveKeyPair.privateKey;
        const publicKey = curveKeyPair.publicKey;
        console.log('Generated Curve25519 key pair.');

        // Save the public key to local storage
        console.log('Saving public key to local storage...');
        await saveKeyToLocalStorage(publicKey, 'publicKey');
        console.log('Public key saved to local storage.');
        // Save keyA, keyB, and privateKey to local storage
        await saveKeyToLocalStorage(keyA, 'keyA');
        await saveKeyToLocalStorage(keyB, 'keyB');
        await saveKeyToLocalStorage(privateKey, 'privateKey');
        console.log('Keys saved to local storage.');

        console.log('Submitting login form...');
        document.getElementById('login-form').submit();
    }catch (error) {
        console.error('Error handling login:', error);
    }
    document.addEventListener("DOMContentLoaded", async function (event) {
        // Call the handleLogin() function here
        await handleLogin();
    });
}
