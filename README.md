# OfflineEncryptor
A plugin that enable vanilla Minecraft's encryption for offline-mode servers, without using Mojang authentication.

---

## What This Plugin Does

This plugin implements the **standard Minecraft encryption protocol (AES/CFB8 + RSA key exchange)** handshaking on servers running in **offline mode**.
Players still skip Mojang authentication, but the connection between client and server becomes encrypted exactly the same way as in **online-mode**.

---

## Features

* Uses vanilla **Minecraft 1.7+ encryption**.
* Does **not** require Mojang session servers.
* Works with any vanilla client.
* Tab player avatar will be available :).

### What this can do (as enable encryption without authentication)
<details>
<summary>Protects sensitive packets</summary>
<br>

* Prevents attackers on the same network from reading:
    * Player chat messages
    * Commands (including `/login`, `/register`)
    * Inventory interactions
    * Movement packet details
* Useful for LAN servers, VPN-shared servers, or hosts where you don’t fully trust the network path.
---
</details>

<details>
<summary>Stops trivial MITM sniffing</summary>
<br>

Encryption blocks attackers from:

* Sniffing passwords sent to login plugin like AuthMe
* Quietly watching traffic in online cafés, dorms, shared Wi-Fi, cloud hosts, etc.
---
</details>

<details>
<summary>What It Does Secure</summary>
<br>

* Prevents sniffing of login passwords (AuthMe, etc.)
* Hides movement, chat, and gameplay packets
* Makes local/LAN attacks harder
* Restores privacy similar to online-mode encryption
---
</details>


### Limitations and traps

<details>
<summary>No actual identity verification</summary>
<br>

This plugin **does not make offline-mode secure against impersonation**.
Because the Mojang `joinServer` authentication step is skipped:

* Anyone can connect using **any username**, including impersonating staff.
* Encryption does **NOT** authenticate who the player really is.

You still need:

* Login plugins like AuthMe / LoginSecurity / similar
* IP / Geo / 2FA plugins if desired
* Proper permission handling
---
</details>

<details>
<summary>Does NOT stop MITM modification</summary>
<br>

Encryption happens *after* the initial handshake.
A skilled attacker can still:

* MITM the first handshake packet
* Downgrade or strip encryption
* Impersonate a player before encryption is negotiated
  Unless players use a secure connection (VPN, SSH tunnel, etc.)
---
</details>

<details>
<summary>False sense of security if misunderstood</summary>
<br>

Some admins may think:

> "We enabled encryption so offline mode is now secure."

This is **incorrect**, authentication and encryption are separate things.

---
</details>

<details>
<summary>Slight CPU overhead</summary>
<br>

AES/CFB8 encryption is not heavy, but on very large servers the extra per-packet cost exists.

---
</details>

<details>
<summary>What it DOES NOT Secure</summary>
<br>

* Offline-mode username spoofing
* Session stealing
* Man-in-the-middle manipulation before encryption begins
* Any kind of real authentication
---
</details>

---


## Summary

If you want **privacy** for an offline-mode server, this plugin helps.
If you want **security against impersonation**, you still need AuthMe (or similar) because **encryption ≠ authentication**.

This plugin is for:

* Server owners wanting encrypted traffic
* Offline/Cracked servers using AuthMe
* Hosts needing protection against local sniffers
* Privacy-conscious LAN/VPN servers

---
## Compatibility & Installation

Just drop it in to plugin folder, nothing need to change, no data will be modified (UUID, username, etc.).

This plugin also support dynamic loading/unloading by PlugManX.

###  Supported Versions

#### **Paper / Purpur / Leaves / Leaf / other Paper-downstream**

* Version: 1.20.5+
* Install **if not using proxy like Velocity**.
* Running it on older versions will fail because encryption logic changed in 1.20.5.

#### **Velocity Proxy**

* Version: 3.4.0+
* Install the plugin **only on Velocity** if Velocity is present.
* **This plugin in the backend server won't operate when using Velocity**

### Client Requirements

* **Minecraft client 1.20.5 or newer**: Mojang changed the handshake and encryption flow in 1.20.5. Older clients cannot complete the encryption sequence. This plugin **will not** send encrypted request packet to players with older versions.
* ViaVersion can down-translate gameplay packets, but **encryption still requires the real client version to be 1.20.5+**.


### Compatibility

| Plugin / Server features                                                                                                   | Compatibility                                                                                        | 
|----------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| [ViaVersion](https://github.com/ViaVersion/ViaVersion)/VB/VR/VFP                                                           | Fully                                                                                                |
| Leaf [PreAuthenticateEvent](https://github.com/Winds-Studio/Leaf/pull/749)                                                 | Fully                                                                                                |
| [LimitedOfflineMode](https://github.com/chank-op/Limited-offline-mode-Paper)                                               | Fully                                                                                                |
| Velocity [PreLoginEvent](https://jd.papermc.io/velocity/3.5.0/com/velocitypowered/api/event/connection/PreLoginEvent.html) | Fully                                                                                                |
| [FastLogin](https://github.com/TuxCoding/FastLogin)                                                                        | Requires [fork version](https://github.com/Lumine1909/FastLogin_Fork/releases/tag/vcompat) on bukkit |

---
**Netty pipeline after installed this plugin with [ViaVersion](https://github.com/ViaVersion/ViaVersion).**
![img.png](img.png)