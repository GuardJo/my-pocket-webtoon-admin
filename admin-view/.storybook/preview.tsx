import type {Preview} from '@storybook/nextjs-vite'
import '../app/globals.css'
import {initialize, mswLoader} from "msw-storybook-addon";
import QueryProvider from "../providers/query-provider";

initialize();

const preview: Preview = {
    loaders: [mswLoader],
    decorators: [
        (Story) => {
            return (
                <QueryProvider>
                    <Story/>
                </QueryProvider>
            )
        }
    ],
    parameters: {
        controls: {
            matchers: {
                color: /(background|color)$/i,
                date: /Date$/i,
            },
        },
        nextjs: {
            appDirectory: true,
        },
        a11y: {
            // 'todo' - show a11y violations in the test UI only
            // 'error' - fail CI on a11y violations
            // 'off' - skip a11y checks entirely
            test: 'todo'
        },
    },
};

export default preview;
