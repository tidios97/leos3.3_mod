const gulp = require('gulp');
const path = require('path');
const tsc = require('gulp-typescript');

/**
 * 
 * @param { name: string } config 
 */
module.exports = function buildTs(config) {

    if (!config.rootDest) {
        throw new Error('Expected the config to contain a property "rootDest"');
    }

    if (config.relativeDest === undefined) {
        throw new Error('Expected the config to contain a property "relativeDest"');
    }

    function toPromise(stream) {
        return new Promise((resolve, reject) => {
            stream.on('finish', () => resolve());
            stream.on('error', (err) => reject(err));
        });
    }

    function staticCopy(src, dest) {
        const stream = gulp.src(src)
            .pipe(gulp.dest(dest));

        return toPromise(stream);
    };

    function compileTs() {

        const targetDirectory = path.resolve(config.rootDest, config.relativeDest);

        const stream = gulp.src(config.src)
            .pipe(tsc({
                target: 'ES2020',
                rootDir: config.rootDir,
                allowJs: true,
                module: 'commonjs',
            }))
            .pipe(gulp.dest(targetDirectory));

        stream.on('error', (err) => console.log(`Error during TS compilation: ${err}`));

        return toPromise(stream);
    }

    const staticCopies = [];

    (config.dependencies || []).forEach(dependency => {

        const targetDirectory = path.resolve(config.rootDest, dependency.relativeDest);

        staticCopies.push(staticCopy(dependency.src, targetDirectory));
    })

    return Promise.all(staticCopies)
        .then(_ => compileTs())

};

