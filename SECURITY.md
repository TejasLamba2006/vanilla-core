# Security Policy

## Supported Versions

We release patches for security vulnerabilities for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.1.x   | :white_check_mark: |
| 1.0.x   | :x:                |

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

If you discover a security vulnerability in Vanilla Core, please report it privately:

### Preferred Method: Security Advisory

1. Go to the [Security Advisory page](https://github.com/TejasLamba2006/smp-core/security/advisories)
2. Click "Report a vulnerability"
3. Fill out the form with details about the vulnerability

### Alternative: Direct Contact

If you prefer, you can also reach out directly:

- **Discord**: Join our [Discord server](https://discord.gg/7fQPG4Grwt) and DM a moderator
- **Email**: Contact through GitHub profile

### What to Include

Please include the following information:

- **Type of vulnerability** (e.g., duplication exploit, permission bypass, etc.)
- **Full paths of source files** related to the vulnerability
- **Location of the affected code** (tag/branch/commit or direct URL)
- **Step-by-step instructions** to reproduce the issue
- **Proof-of-concept or exploit code** (if possible)
- **Impact of the vulnerability** and what an attacker could do

### Response Timeline

- We will acknowledge your report within **48 hours**
- We will provide a detailed response within **7 days**, including the next steps
- We will keep you informed about the progress toward a fix
- We will notify you when the vulnerability is fixed

### After the Fix

- We will release a security update as soon as possible
- We will credit you in the CHANGELOG (unless you prefer to remain anonymous)
- We encourage responsible disclosure and will work with you on the disclosure timeline

## Security Best Practices for Users

When using Vanilla Core:

1. **Keep Updated**: Always use the latest version
2. **Review Permissions**: Carefully configure command permissions
3. **Test Configurations**: Test config changes on a test server first
4. **Monitor Logs**: Regularly check logs for unusual activity
5. **Backup Regularly**: Keep regular backups of your server

## Known Security Considerations

- **OP Permissions**: Commands with `smpcore.reload` can modify server behavior
- **WorldGuard Integration**: Ensure WorldGuard permissions are properly configured
- **Config Access**: Protect config files from unauthorized access

---

Thank you for helping keep Vanilla Core and its users safe!
