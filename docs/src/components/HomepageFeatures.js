import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
    {
        title: 'Simple like Junit 4',
//        Svg: require('../../static/img/undraw_docusaurus_mountain.svg').default,
        description: (
                <>
                    Failgood is as easy to understand as junit 4.
                    Every test runs in a fresh context.
                </>
        ),
    },
    {
        title: 'Just like RSpec',
//        Svg: require('../../static/img/undraw_docusaurus_tree.svg').default,
        description: (
                <>
                    Failgood
                </>
        ),
    },
    {
        title: 'Pretty Fast',
//        Svg: require('../../static/img/undraw_docusaurus_react.svg').default,
        description: (
                <>
                    A test suite needs to be fast. Failgood's own suite runs in less than a second. Yours can be that
                    fast too
                </>
        ),
    },
];

// noinspection JSUnusedLocalSymbols
function Feature({Svg, title, description}) {
    return (
            <div className={clsx('col col--4')}>
                {/*
                <div className="text--center">
                    <Svg className={styles.featureSvg} alt={title}/>
                </div>
*/}
                <div className="text--center padding-horiz--md">
                    <h3>{title}</h3>
                    <p>{description}</p>
                </div>
            </div>
    );
}

export default function HomepageFeatures() {
    return (
            <section className={styles.features}>
                <div className="container">
                    <div className="row">
                        {FeatureList.map((props, idx) => (
                                <Feature key={idx} {...props} />
                        ))}
                    </div>
                </div>
            </section>
    );
}
