# Cryptanalysis Helper
## ++A compact and portable offline tool for cryptanalysis++

*CryptanalysisHelper* is a small utility written in pure Java 8, that can help with basic cryptanalysis. Only supports **English**.

### The current features are:

  **1. Shift Cipher**
  - Encrypt single plaintext
  - Decrypt single plaintext
  - Brute force the key (i.e. shift=5)
    
  **2. Simple XOR Cipher (can handle input of any Unicode codepoint)**
  - Decrypt single plaintext automatically.
  - Decrypt single plaintext that uses 2 keystreams (alternately) automatically, but keystream-1 must be provided.
  - Brute force all possible values for keystream-2, by providing keystream-1.
  - Encrypt single plaintext with 1 or 2 keysteams (**COMING SOON**)

  **3. Columnar Transposition Cipher (can handle keys with length up to 10 characters)**
  - Decrypt cipher and guess key automatically.
  - Brute force all possible permutations of plaintext.
  
 **4. Frequency Analysis**
  - View statistics about the English language and also about the cipher that is currently being analysed.
  - Perform substitutions in real time.
  - Save progress (**COMING SOON**)
  - Ability to open semi-complete analysis. User can provide partial key (**COMING SOON**)
  
 **5. Substitution Cipher (COMING SOON)** *-both monoalphabetic and polyalphabetic*
  - Decrypt a substitution cipher and guess the key automatically (**COMING SOON**)
  - Encrypt a single plaintext as a substitution cipher (either by providing the key or by randomly generating one) (**COMING SOON**)
  
 **6. Can store results to disk**
