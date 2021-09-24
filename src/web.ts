import { WebPlugin } from '@capacitor/core';

import type { FaceMePlugin } from './definitions';

export class FaceMeWeb extends WebPlugin implements FaceMePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async inizialize(licenseKey: string): Promise<string> {
    console.log('inizialize: ', licenseKey);
    return licenseKey;
  }
}
