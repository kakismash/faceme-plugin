import { WebPlugin } from '@capacitor/core';

import type { FacemePlugin } from './definitions';

export class FacemeWeb extends WebPlugin implements FacemePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async inizialize(licenseKey: string): Promise<string> {
    return licenseKey;
  }
}
