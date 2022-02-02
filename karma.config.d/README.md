
Karma config
============  

Karma configuration files for JS legacy tests go in this `karma.config.d` directory[^1].

Local config
------------  

`.gitignore` is configured to ignore `local.*` in this directory so that configuration can be added here without adding it to the project itself.

### Example

Karma uses `ChromeHeadless` as its default execution environment, and that works very nicely with a CI environment that has it installed, such as Github Actions with `ubuntu-latest`. I myself prefer to use Firefox and have no intention of putting Chrome on my personal machine, but configuring the test task in Gradle to `useFirefox()` causes the CI to fail.

Instead, we can leave Karma's configuration as default in Gradle, then use a local Karma configuration in this directory to suggest that it use Firefox instead.

I used `npm` to install `karma-firefox-launcher` locally. Then, I added `local.use-firefox.karma.conf.js`:

```javascript 
config.plugins.push("karma-firefox-launcher");  
config.set({"browsers":["Firefox"]});  
```  

This file stays out of the repo, so now the legacy JS tests work in both places.

[^1]: See [Run tests in Kotlin/JS](https://kotlinlang.org/docs/js-running-tests.html) for details.
